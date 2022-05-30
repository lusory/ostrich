package me.lusory.ostrich.qapi.metadata.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    Class<?> responseType();

    boolean respondsWithArray() default false;

    /**
     * Normally, the QAPI schema is used to describe synchronous exchanges, where a response is expected.
     * But in some cases, the action of a command is expected to change state in a way that a successful response is not possible (although the command will still return an error object on failure).
     * When a successful reply is not possible, the command definition includes the optional member ‘success-response’ with boolean value false.
     * So far, only QGA makes use of this member.
     */
    boolean successResponse() default true;

    /**
     * Member ‘allow-oob’ declares whether the command supports out-of-band (OOB) execution.
     * See qmp-spec.txt for out-of-band execution syntax and semantics.
     */
    boolean allowOob() default false;

    /**
     * Member ‘allow-preconfig’ declares whether the command is available before the machine is built.
     * QMP is available before the machine is built only when QEMU was started with –preconfig.
     */
    boolean allowPreconfig() default false;

    /**
     * Member ‘coroutine’ tells the QMP dispatcher whether the command handler is safe to be run in a coroutine.
     * If it is true, the command handler is called from coroutine context and may yield while waiting for an external event (such as I/O completion) in order to avoid blocking the guest and other background operations.
     */
    boolean coroutine() default false;
}
