package me.lusory.ostrich.qapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.lusory.ostrich.qapi.jackson.QUnionDeserializer;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = QUnionDeserializer.class)
public interface QUnion extends QType {
    QEnum getDiscriminator();
}
