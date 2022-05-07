package me.lusory.ostrich.qapi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.lusory.ostrich.qapi.jackson.QAlternateDeserializer;
import me.lusory.ostrich.qapi.jackson.QAlternateSerializer;

@JsonSerialize(using = QAlternateSerializer.class)
@JsonDeserialize(using = QAlternateDeserializer.class)
public interface QAlternate {
    Object getValue();
}
