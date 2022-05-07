package me.lusory.ostrich.qapi.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.SneakyThrows;
import me.lusory.ostrich.qapi.QAlternate;

import java.io.IOException;

public class QAlternateDeserializer extends JsonDeserializer<QAlternate> {
    @Override
    public QAlternate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final Class<?> type = ctxt.getContextualType().getRawClass();

        for (final Class<?> klass : getTypes(type)) {
            try {
                return (QAlternate) fastInvokeOf(klass, p.readValueAs(getRealType(klass)));
            } catch (IOException ignored) {
                // ignored
            }
        }
        throw new RuntimeException("Could not deserialize " + p.getValueAsString() + " to " + type.getName());
    }

    @SneakyThrows
    private static Class<?> getRealType(Class<?> klass) {
        return klass.getDeclaredField("value").getType();
    }

    @SneakyThrows
    private static Object fastInvokeOf(Class<?> klass, Object arg) {
        return klass.getDeclaredMethod("of", arg.getClass()).invoke(null, arg);
    }

    @SneakyThrows
    private static Class<?>[] getTypes(Class<?> klass) {
        return (Class<?>[]) klass.getField("TYPES").get(null);
    }
}
