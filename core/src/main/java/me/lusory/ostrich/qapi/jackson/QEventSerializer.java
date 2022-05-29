package me.lusory.ostrich.qapi.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import me.lusory.ostrich.qapi.QEvent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class QEventSerializer extends JsonSerializer<QEvent<?>> {
    @Override
    public void serialize(QEvent<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        try {
            gen.writeStringField("event", (String) value.getClass().getDeclaredMethod("getRawName").invoke(null));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        if (value.getData() != null || value.getData().getClass().getDeclaredFields().length != 0) {
            gen.writeObjectField("data", value.getData());
        }
        gen.writeObjectFieldStart("timestamp");
        gen.writeNumberField("seconds", value.getTimestamp().getEpochSecond());
        gen.writeNumberField("microseconds", value.getTimestamp().getNano() / 1000);
        gen.writeEndObject();
        gen.writeEndObject();
    }
}
