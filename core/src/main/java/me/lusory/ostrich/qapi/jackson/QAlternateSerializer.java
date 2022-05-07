package me.lusory.ostrich.qapi.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import me.lusory.ostrich.qapi.QAlternate;

import java.io.IOException;

public class QAlternateSerializer extends JsonSerializer<QAlternate> {
    @Override
    public void serialize(QAlternate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObject(value.getValue());
    }
}
