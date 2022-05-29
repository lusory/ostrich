package me.lusory.ostrich.qapi.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import me.lusory.ostrich.qapi.QEvent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;

public class QEventDeserializer extends JsonDeserializer<QEvent> {
    @Override
    public QEvent deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final JsonNode node = p.readValueAsTree();
        final JsonNode timestampNode = node.get("timestamp");
        final Class<?> type = ctxt.getContextualType().getRawClass();

        final Instant timestamp = Instant.ofEpochSecond(
                timestampNode.get("seconds").asLong(),
                timestampNode.get("microseconds").asLong() * 1000
        );

        try {
            final Class<?> dataType = type.getDeclaredField("data").getType();

            return (QEvent) type.getDeclaredConstructor(Instant.class, dataType).newInstance(timestamp, p.getCodec().treeToValue(node.get("data"), dataType));
        } catch (NoSuchFieldException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
