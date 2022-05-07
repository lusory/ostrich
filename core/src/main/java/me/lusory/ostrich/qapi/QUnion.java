package me.lusory.ostrich.qapi;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface QUnion {
    QEnum getDiscriminator();
}
