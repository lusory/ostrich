package me.lusory.ostrich.qapi.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import me.lusory.ostrich.qapi.QAlternate;
import me.lusory.ostrich.qapi.metadata.annotations.Alternate;
import me.lusory.ostrich.qapi.metadata.annotations.Alternative;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

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
        if (type == null) {
            throw new IOException("QAlternateDeserializer must always be contextual");
        }
        final Alternate alternate = type.getAnnotation(Alternate.class);
        if (alternate == null) {
            throw new IOException("Invalid alternate target (" + type.getName() + "), missing @Alternate annotation");
        }
        for (final Class<?> alternativeKlass : alternate.alternatives()) {
            final Alternative alternative = alternativeKlass.getAnnotation(Alternative.class);

            try {
                Object value;
                if (alternative.array()) {
                    value = ctxt.readValue(
                            p,
                            ctxt.getTypeFactory().constructCollectionLikeType(List.class, alternative.type())
                    );
                } else {
                    value = ctxt.readValue(p, alternative.type());
                }

                final Constructor<?> ctor = alternativeKlass.getDeclaredConstructor(alternative.array() ? List.class : alternative.type());
                ctor.setAccessible(true);

                return (QAlternate) ctor.newInstance(value);
            } catch (IOException ignored) {
                // ignored
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Could not deserialize " + p.getValueAsString() + " to " + type.getName());
    }
}
