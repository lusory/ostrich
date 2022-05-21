package me.lusory.ostrich.process;

import me.lusory.ostrich.process.util.MiscUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class CheckImageBuilder implements ImageBuilder {
    private ImageFormat fmt = ImageFormat.RAW;
    private File filename;

    public CheckImageBuilder(ImageFormat fmt, File filename) {
        this.fmt = fmt;
        this.filename = filename;
    }

    public CheckImageBuilder() {
    }

    @Override
    public String[] getArguments() {
        return new String[] { "-f", Objects.requireNonNull(fmt).toString(), Objects.requireNonNull(filename).getAbsolutePath() };
    }

    @Override
    public Process build(File executable) throws IOException {
        return new ProcessBuilder(MiscUtils.concat(new String[] { executable.getAbsolutePath() }, getArguments())).start();
    }

    public ImageFormat getFmt() {
        return this.fmt;
    }

    public File getFilename() {
        return this.filename;
    }

    public void setFmt(ImageFormat fmt) {
        this.fmt = fmt;
    }

    public void setFilename(File filename) {
        this.filename = filename;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof CheckImageBuilder)) return false;
        final CheckImageBuilder other = (CheckImageBuilder) o;
        if (!other.canEqual(this)) return false;
        final Object this$fmt = this.getFmt();
        final Object other$fmt = other.getFmt();
        if (!Objects.equals(this$fmt, other$fmt)) return false;
        final Object this$filename = this.getFilename();
        final Object other$filename = other.getFilename();
        return Objects.equals(this$filename, other$filename);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof CheckImageBuilder;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $fmt = this.getFmt();
        result = result * PRIME + ($fmt == null ? 43 : $fmt.hashCode());
        final Object $filename = this.getFilename();
        result = result * PRIME + ($filename == null ? 43 : $filename.hashCode());
        return result;
    }

    public String toString() {
        return "CheckImageBuilder(fmt=" + this.getFmt() + ", filename=" + this.getFilename() + ")";
    }
}
