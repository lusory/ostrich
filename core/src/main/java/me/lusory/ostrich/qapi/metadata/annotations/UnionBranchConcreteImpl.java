package me.lusory.ostrich.qapi.metadata.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface UnionBranchConcreteImpl {
    String discriminator();
    Class<?> clazz();
}
