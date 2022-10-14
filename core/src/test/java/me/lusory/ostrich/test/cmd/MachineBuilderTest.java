package me.lusory.ostrich.test.cmd;

import me.lusory.ostrich.cmd.MachineBuilder;
import me.lusory.ostrich.test.util.MiscUtils;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MachineBuilderTest {
    @Test
    public void simpleArguments() {
        final String[] expected = new String[] {
                "-m", "1024",
                "-enable-kvm",
                "-vnc",
                ":0,to=59635,websocket=on"
        };
        final String[] actual = new MachineBuilder()
                .m("1024")
                .enableKvm()
                .vnc(":0,to=59635,websocket=on")
                .build();

        MiscUtils.assertArrayEqualsUnordered(expected, actual);
    }

    @Test
    public void complexArguments() {
        final UUID uuid = UUID.randomUUID();

        final StringBuilder driveArgBuilder = new StringBuilder();
        final Map<UUID, String> drives = Stream.generate(UUID::randomUUID)
                .limit(25)
                .map(drive -> {
                    driveArgBuilder.setLength(0);

                    driveArgBuilder.append("node-name=");
                    driveArgBuilder.append(MiscUtils.takeChars('n' + drive.toString(), 31)); // read the javadoc of BlockdevOptions
                    driveArgBuilder.append(",format=");
                    driveArgBuilder.append(ThreadLocalRandom.current().nextBoolean() ? "QCOW2" : "RAW");
                    driveArgBuilder.append(",media=");
                    driveArgBuilder.append(ThreadLocalRandom.current().nextBoolean() ? "CDROM" : "DISK");
                    driveArgBuilder.append(",readonly=");
                    driveArgBuilder.append(ThreadLocalRandom.current().nextBoolean());
                    driveArgBuilder.append(",file=");
                    driveArgBuilder.append(MiscUtils.createTempFile("qemu-drive", "test"));

                    return new AbstractMap.SimpleImmutableEntry<>(drive, driveArgBuilder.toString());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final MachineBuilder builder = new MachineBuilder()
                .name(uuid.toString())
                .uuid(uuid.toString())
                .enableKvm()
                .m("1024")
                .qmp("unix:" + uuid + ".sock,server,wait=off")
                .vnc(":0,to=59635,websocket=on");

        for (final String imageStr : drives.values()) {
            builder.drive(imageStr);
        }

        final String[] expected = Stream.concat(
                Arrays.stream(new String[] {
                        "-name", uuid.toString(),
                        "-uuid", uuid.toString(),
                        "-enable-kvm",
                        "-m", "1024",
                        "-qmp", "unix:" + uuid + ".sock,server,wait=off",
                        "-vnc", ":0,to=59635,websocket=on"
                }),
                drives.values().stream()
                        .flatMap(driveStr -> Stream.of("-drive", driveStr))
        ).toArray(String[]::new);
        final String[] actual = builder.build();

        MiscUtils.assertArrayEqualsUnordered(expected, actual);
    }
}
