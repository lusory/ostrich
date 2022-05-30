package me.lusory.ostrich.qapi.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import me.lusory.ostrich.qapi.QCommand;
import me.lusory.ostrich.qapi.metadata.annotations.RawName;

import java.io.IOException;

public class QCommandSerializer extends JsonSerializer<QCommand<?, ?>> {
    @Override
    public void serialize(QCommand<?, ?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("execute", value.getClass().getAnnotation(RawName.class).value());
        if (value.getData() != null) {
            gen.writeObjectField("arguments", value.getData());
        }
        gen.writeEndObject();
    }
}
