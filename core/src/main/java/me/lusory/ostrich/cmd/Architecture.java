package me.lusory.ostrich.cmd;

import java.util.Locale;

// https://www.qemu.org/docs/master/system/targets.html
public enum Architecture {
    AARCH64,
    ARM,
    AVR,
    M68K,
    MIPS,
    MIPSEL,
    MIPS64,
    MIPS64EL,
    PPC64,
    RISCV64,
    RISCV32,
    RX,
    S390X,
    SPARC,
    SPARC64,
    X86_64,
    XTENSA,
    XTENSAEB;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String getBinaryName() {
        return "qemu-system-" + this;
    }
}
