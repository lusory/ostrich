package me.lusory.ostrich.qapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.lusory.ostrich.qapi.control.QmpCapabilitiesCommand;
import me.lusory.ostrich.qapi.exceptions.QAPIException;
import me.lusory.ostrich.qapi.exceptions.QAPISocketException;
import me.lusory.ostrich.qapi.metadata.annotations.Command;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class QAPISocket {
    protected static final ObjectMapper MAPPER = new ObjectMapper();
    protected final BufferedReader input;
    protected final Writer output;
    protected final Thread readThread;
    protected final BlockingQueue<JsonNode> readQueue = new ArrayBlockingQueue<>(1);

    public QAPISocket(Socket sock) {
        if (!sock.isConnected() || sock.isClosed()) {
            throw new QAPISocketException("Socket is not connected");
        }
        // prepare data streams for each direction
        try {
            this.input = new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.ISO_8859_1));
            this.output = new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            throw new QAPISocketException("Could not acquire a bidirectional connection", e);
        }
        try {
            // negotiate the capabilities
            final QmpCapabilitiesCommand.Data data = negotiate(MAPPER.readValue(read0(), QMPGreeting.class));
            final QmpCapabilitiesCommand cmd = new QmpCapabilitiesCommand();
            if (data != null) {
                cmd.setData(data);
            }

            // send the qmp_capabilities command
            write0(cmd);

            // read the response, throw if an error occurred
            final JsonNode response = MAPPER.readTree(read0());
            if (response.has("error")) {
                throw new QAPIException(response.get("error").toString());
            }
        } catch (IOException e) {
            throw new QAPISocketException("An error occurred while negotiating", e);
        }

        // set up the reading loop
        readThread = new Thread(() -> {
            while (!sock.isClosed()) {
                try {
                    final JsonNode value = MAPPER.readTree(read0());

                    if (value.has("event")) {
                        handleEvent(deserializeEvent0(value));
                    } else {
                        readQueue.add(value);
                    }
                } catch (IOException e) {
                    throw new QAPISocketException("An error occurred while reading", e);
                }
            }
        });

        readThread.start();
    }

    protected String read0() throws IOException {
        final String line = input.readLine();
        if (line == null) {
            throw new EOFException();
        }
        return line;
    }

    protected void write0(Object value) throws IOException {
        output.write(MAPPER.writeValueAsString(value));
        output.write("\n");
        output.flush();
    }

    protected QEvent<?> deserializeEvent0(JsonNode node) throws IOException {
        final Class<?> eventClass = Events.TYPES.get(node.get("event").asText());
        if (eventClass == null) {
            throw new QAPIException("Unknown event " + node);
        }

        return (QEvent<?>) MAPPER.treeToValue(node, eventClass);
    }

    // public API below

    @SuppressWarnings("unchecked")
    public synchronized <R> R sendCommand(QCommand<?, R> cmd) throws IOException, InterruptedException {
        write0(cmd);

        final JsonNode response = readQueue.take();
        if (response.has("error")) {
            throw new QAPIException(response.get("error").toString());
        }

        final Command meta = cmd.getClass().getAnnotation(Command.class);
        if (meta.respondsWithArray()) {
            return MAPPER.treeToValue(
                    response.get("return"),
                    MAPPER.getTypeFactory().constructCollectionLikeType(List.class, meta.responseType())
            );
        }
        return (R) MAPPER.treeToValue(response.get("return"), meta.responseType());
    }

    // supposed to be overridden

    protected @Nullable QmpCapabilitiesCommand.Data negotiate(QMPGreeting greeting) {
        return null;
    }

    protected void handleEvent(QEvent<?> event) {
    }
}
