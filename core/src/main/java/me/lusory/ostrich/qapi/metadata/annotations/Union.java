package me.lusory.ostrich.qapi.metadata.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Union {
    String condition() default "";
    Feature[] features() default {};
    String discriminator();
    UnionBranchConcreteImpl[] branches();
}
