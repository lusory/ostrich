package me.lusory.ostrich.qapi;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.lusory.ostrich.qapi.jackson.QCommandSerializer;

@SuppressWarnings("unused") // suppress unused type variable, we need it for response deserialization
@JsonSerialize(using = QCommandSerializer.class)
public interface QCommand<D extends QType, R> extends QType {
    default D getData() {
        return null;
    }

    default void setData(D data) {
    }
}
