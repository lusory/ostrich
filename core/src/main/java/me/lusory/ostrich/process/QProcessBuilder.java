package me.lusory.ostrich.process;

import java.io.File;
import java.io.IOException;

public interface QProcessBuilder {
    String[] getArguments();

    Process build(File executable) throws IOException;

    default int buildAndBlock(File executable) throws IOException, InterruptedException {
        return build(executable).waitFor();
    }
}
