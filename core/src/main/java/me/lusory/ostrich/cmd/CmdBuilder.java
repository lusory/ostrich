package me.lusory.ostrich.cmd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface CmdBuilder {
    String[] build();

    default Process start(String executablePath) throws IOException {
        final List<String> command = new ArrayList<>();
        command.add(executablePath);
        command.addAll(Arrays.asList(build()));

        return new ProcessBuilder(command).start();
    }
}
