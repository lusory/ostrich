package me.lusory.ostrich.qapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.lusory.ostrich.qapi.block_core.ImageInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@UtilityClass
public class DeserializationUtils {
    public final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Reads an {@link ImageInfo} object from the stdout of a Process.<br>
     * The process is most likely going to be acquired from a {@link ProcessBuilder} with arguments from {@link me.lusory.ostrich.cmd.InfoImageBuilder}.
     * <p>
     * <strong>The process must have the <code>--output</code> argument set to <code>json</code> in the case of using {@link me.lusory.ostrich.cmd.InfoImageBuilder}!</strong><br>
     *
     * <p><strong>Implementation note</strong></p>
     * All line breaks are converted to {@literal \n} before deserialization.
     *
     * @param process the process which stdout will be read
     * @return the {@link ImageInfo} object
     * @throws IOException if the JSON deserialization failed
     */
    @SneakyThrows(InterruptedException.class)
    public ImageInfo readImageInfo(Process process) throws IOException {
        process.waitFor();
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            final String output = reader.lines().collect(Collectors.joining("\n")).trim();

            return MAPPER.readValue(output, ImageInfo.class);
        }
    }
}
