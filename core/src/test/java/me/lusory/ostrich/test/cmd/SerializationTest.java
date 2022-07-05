package me.lusory.ostrich.test.cmd;

import lombok.SneakyThrows;
import me.lusory.ostrich.cmd.MachineBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SerializationTest {
    @Test
    @SneakyThrows
    public void serialize() {
        Assertions.assertArrayEquals(
                new String[] { "-machine", "help", "-cpu", "help" },
                new MachineBuilder()
                        .machine("help")
                        .cpu("help")
                        .build()
        );
    }
}
