package me.lusory.ostrich.qapi.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import me.lusory.ostrich.qapi.QUnion;
import me.lusory.ostrich.qapi.metadata.annotations.Union;
import me.lusory.ostrich.qapi.metadata.annotations.UnionBranchConcreteImpl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class QUnionDeserializer extends JsonDeserializer<QUnion> implements ContextualDeserializer {
    private Class<?> type;

    public QUnionDeserializer(Class<?> type) {
        this.type = type;
    }

    public QUnionDeserializer() {
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) {
        return new QUnionDeserializer(deserializationContext.getContextualType().getRawClass());
    }

    @Override
    public QUnion deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (type == null) {
            throw new IOException("QUnionDeserializer must always be contextual");
        }

        final Union union = type.getAnnotation(Union.class);
        if (union == null) {
            throw new IOException("Invalid union target (" + type.getName() + "), missing @Union annotation");
        }

        final JsonNode node = p.readValueAsTree();
        final String valueDiscriminator = node.get(union.discriminator()).asText().toLowerCase(Locale.ROOT);
        final UnionBranchConcreteImpl branch = Arrays.stream(union.branches())
                .filter(impl -> impl.discriminator().equals(valueDiscriminator))
                .findFirst()
                .orElseThrow(() -> new IOException("No branch found for discriminator " + valueDiscriminator));

        return (QUnion) ctxt.readTreeAsValue(node, branch.clazz());
    }
}
