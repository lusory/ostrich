package me.lusory.ostrich.process;

import java.util.Locale;

public interface ImageBuilder extends QProcessBuilder {
    enum ImageFormat {
        /**
         * Raw disk image format (default).
         *
         * This format has the advantage of being simple and easily exportable to all other emulators.
         * If your file system supports holes (for example in ext2 or ext3 on Linux or NTFS on Windows), then only the written sectors will reserve space.
         */
        RAW,

        /**
         * Host device format.
         *
         * This format should be used instead of raw when converting to block devices or other devices where "holes" are not supported.
         */
        HOST_DEVICE,

        /**
         * QEMU image format, the most versatile format.
         *
         * Use it to have smaller images (useful if your filesystem does not support holes, for example on Windows), optional AES encryption, zlib based compression and support of multiple VM snapshots.
         * <p>
         * Supported options:
         * <ul>
         *     <li>"backing_file" - File name of a base image (see create subcommand)</li>
         *     <li>"backing_fmt" - Image format of the base image</li>
         *     <li>"encryption" - If this option is set to "on", the image is encrypted. Encryption uses the AES format which is very secure (128 bit keys). Use a long password (16 characters) to get maximum protection.</li>
         *     <li>"cluster_size" - Changes the qcow2 cluster size (must be between 512 and 2M). Smaller cluster sizes can improve the image file size whereas larger cluster sizes generally provide better performance.</li>
         *     <li>"preallocation" - Preallocation mode (allowed values: off, metadata, full). An image with preallocated metadata is initially larger but can improve performance when the image needs to grow. Full preallocation additionally writes zeros to the whole image in order to preallocate lower layers (e.g. the file system containing the image file) as well. Note that full preallocation writes to every byte of the virtual disk, so it can take a long time for large images.</li>
         * </ul>
         */
        QCOW2,

        /**
         * Old QEMU image format.
         *
         * Left for compatibility.
         * <p>
         * Supported options:
         * <ul>
         *     <li>"backing_file" - File name of a base image (see create subcommand)</li>
         *     <li>"encryption" - If this option is set to "on", the image is encrypted.</li>
         * </ul>
         */
        QCOW,

        /**
         * User Mode Linux Copy On Write image format.
         *
         * Used to be the only growable image format in QEMU.
         * It is supported only for compatibility with previous versions.
         * It does not work on win32.
         */
        COW,

        /**
         * VirtualBox 1.1 compatible image format.
         */
        VDI,

        /**
         * VMware 3 and 4 compatible image format.
         * <p>
         * Supported options:
         * <ul>
         *     <li>"backing_fmt" - Image format of the base image</li>
         *     <li>"compat6" - Create a VMDK version 6 image (instead of version 4)</li>
         * </ul>
         */
        VMDK,

        /**
         * VirtualPC compatible image format (VHD).
         */
        VPC,

        /**
         * Linux Compressed Loop image, useful only to reuse directly compressed CD-ROM images present for example in the Knoppix CD-ROMs.
         */
        CLOOP;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
