package me.lusory.ostrich.test.qapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.lusory.ostrich.qapi.block_core.ImageInfo;
import me.lusory.ostrich.qapi.block_core.ImageInfoSpecificQCow2;
import me.lusory.ostrich.qapi.block_core.ImageInfoSpecificQcow2Branch;
import me.lusory.ostrich.qapi.control.QmpCapabilitiesCommand;
import me.lusory.ostrich.qapi.qdev.DeviceAddCommand;
import me.lusory.ostrich.qapi.sockets.NetworkAddressFamily;
import me.lusory.ostrich.qapi.ui.SpiceBasicInfo;
import me.lusory.ostrich.qapi.ui.SpiceConnectedEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;

public class SerializationTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void serialize() throws IOException {
        Assertions.assertEquals(
                "{\"execute\":\"qmp_capabilities\"}",
                MAPPER.writeValueAsString(new QmpCapabilitiesCommand())
        );
        Assertions.assertEquals(
                "{\"execute\":\"device_add\",\"arguments\":{\"driver\":\"test\",\"bus\":\"test\",\"id\":\"test\",\"test\":\"IPV4\"}}",
                MAPPER.writeValueAsString(
                        new DeviceAddCommand(new DeviceAddCommand.Data("test", "test", "test", new HashMap<String, Object>() {
                            {
                                put("test", NetworkAddressFamily.IPV4);
                            }
                        }))
                )
        );
        final Instant time = Instant.now();
        Assertions.assertEquals(
                "{\"event\":\"SPICE_CONNECTED\",\"data\":{\"server\":{\"host\":\"localhost\",\"port\":\"1111\",\"family\":\"IPV4\"},\"client\":{\"host\":\"192.0.0.1\",\"port\":\"1111\",\"family\":\"IPV4\"}},\"timestamp\":{\"seconds\":" + time.getEpochSecond() + ",\"microseconds\":" + time.getNano() / 1000 + "}}",
                MAPPER.writeValueAsString(new SpiceConnectedEvent(
                        time,
                        new SpiceConnectedEvent.Data(
                                new SpiceBasicInfo("localhost", "1111", NetworkAddressFamily.IPV4),
                                new SpiceBasicInfo("192.0.0.1", "1111", NetworkAddressFamily.IPV4)
                        )
                ))
        );
        final ImageInfo info = new ImageInfo();
        info.setFilename("test");
        info.setFormat("QCOW2");
        info.setVirtualSize(1024);
        final ImageInfoSpecificQCow2 specific = new ImageInfoSpecificQCow2();
        specific.setCompat("test");
        specific.setRefcountBits(1024);
        info.setFormatSpecific(new ImageInfoSpecificQcow2Branch(specific));
        Assertions.assertEquals(
                "{\"filename\":\"test\",\"format\":\"QCOW2\",\"virtual-size\":1024,\"format-specific\":{\"type\":\"QCOW2\",\"data\":{\"compat\":\"test\",\"refcount-bits\":1024}}}",
                MAPPER.writeValueAsString(info)
        );
    }

    @Test
    public void deserialize() throws IOException {
        final Instant time = Instant.now();
        Assertions.assertEquals(
                new SpiceConnectedEvent(
                        time,
                        new SpiceConnectedEvent.Data(
                                new SpiceBasicInfo("localhost", "1111", NetworkAddressFamily.IPV4),
                                new SpiceBasicInfo("192.0.0.1", "1111", NetworkAddressFamily.IPV4)
                        )
                ),
                MAPPER.readValue(
                        "{\"event\":\"SPICE_CONNECTED\",\"data\":{\"server\":{\"host\":\"localhost\",\"port\":\"1111\",\"family\":\"IPV4\"},\"client\":{\"host\":\"192.0.0.1\",\"port\":\"1111\",\"family\":\"IPV4\"}},\"timestamp\":{\"seconds\":" + time.getEpochSecond() + ",\"microseconds\":" + time.getNano() / 1000 + "}}",
                        SpiceConnectedEvent.class
                )
        );
        final ImageInfo info = new ImageInfo();
        info.setFilename("test");
        info.setFormat("QCOW2");
        info.setVirtualSize(1024);
        final ImageInfoSpecificQCow2 specific = new ImageInfoSpecificQCow2();
        specific.setCompat("test");
        specific.setRefcountBits(1024);
        info.setFormatSpecific(new ImageInfoSpecificQcow2Branch(specific));
        Assertions.assertEquals(
                info,
                MAPPER.readValue(
                        "{\"filename\":\"test\",\"format\":\"QCOW2\",\"virtual-size\":1024,\"format-specific\":{\"type\":\"QCOW2\",\"data\":{\"compat\":\"test\",\"refcount-bits\":1024}}}",
                        ImageInfo.class
                )
        );
    }
}
