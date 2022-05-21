package me.lusory.ostrich.process;

import java.io.File;
import java.io.IOException;

public interface QProcessBuilder {
    /**
     * Assembles the process arguments based on the builder contents.
     *
     * @return the arguments, not including the executable
     */
    String[] getArguments();

    /**
     * Launches the supplied executable with the arguments from {@link #getArguments()}.
     *
     * @param executable the executable
     * @return the process
     * @throws IOException when a process I/O error occurs
     */
    Process build(File executable) throws IOException;

    /**
     * Launches the supplied executable with the arguments from {@link #getArguments()} and waits until the process exits.
     *
     * @param executable the executable
     * @return the process return code
     * @throws IOException when a process I/O error occurs
     * @throws InterruptedException when the current thread is interrupted by another thread while it is waiting
     */
    default int buildAndBlock(File executable) throws IOException, InterruptedException {
        return build(executable).waitFor();
    }
}
