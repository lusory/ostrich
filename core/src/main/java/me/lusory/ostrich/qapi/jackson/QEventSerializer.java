package me.lusory.ostrich.qapi.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import me.lusory.ostrich.qapi.QEvent;
import me.lusory.ostrich.qapi.metadata.annotations.RawName;

import java.io.IOException;

public class QEventSerializer extends JsonSerializer<QEvent<?>> {
    @Override
    public void serialize(QEvent<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("event", value.getClass().getAnnotation(RawName.class).value());
        if (value.getData() != null) {
            gen.writeObjectField("data", value.getData());
        }
        gen.writeObjectFieldStart("timestamp");
        gen.writeNumberField("seconds", value.getTimestamp().getEpochSecond());
        gen.writeNumberField("microseconds", value.getTimestamp().getNano() / 1000);
        gen.writeEndObject();
        gen.writeEndObject();
    }
}
