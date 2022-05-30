package me.lusory.ostrich.qapi.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import me.lusory.ostrich.qapi.QAlternate;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class QAlternateDeserializer extends JsonDeserializer<QAlternate> implements ContextualDeserializer {
    private Class<?> type;

    public QAlternateDeserializer(Class<?> type) {
        this.type = type;
    }

    public QAlternateDeserializer() {
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) {
        return new QAlternateDeserializer(deserializationContext.getContextualType().getRawClass());
    }

    @Override
    public QAlternate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            for (final Class<?> klass : (Class<?>[]) type.getField("TYPES").get(null)) {
                try {
                    final Object value = p.readValueAs(klass.getDeclaredField("value").getType());

                    return (QAlternate) klass.getDeclaredMethod("of", value.getClass()).invoke(null, value);
                } catch (IOException ignored) {
                    // ignored
                } catch (NoSuchFieldException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Could not deserialize " + p.getValueAsString() + " to " + type.getName());
    }
}
