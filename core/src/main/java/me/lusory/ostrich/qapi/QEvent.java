package me.lusory.ostrich.qapi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.lusory.ostrich.qapi.jackson.QEventDeserializer;
import me.lusory.ostrich.qapi.jackson.QEventSerializer;

import java.time.Instant;

@JsonSerialize(using = QEventSerializer.class)
@JsonDeserialize(using = QEventDeserializer.class)
public interface QEvent<T extends QStruct> extends QType {
    default T getData() {
        return null;
    }

    default void setData(T data) {
    }

    Instant getTimestamp();
}
