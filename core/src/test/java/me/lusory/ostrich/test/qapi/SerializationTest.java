package me.lusory.ostrich.test.qapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.lusory.ostrich.qapi.control.QmpCapabilitiesCommand;
import me.lusory.ostrich.qapi.qdev.DeviceAddCommand;
import me.lusory.ostrich.qapi.sockets.NetworkAddressFamily;
import me.lusory.ostrich.qapi.ui.SpiceBasicInfo;
import me.lusory.ostrich.qapi.ui.SpiceConnectedEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;

public class SerializationTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @SneakyThrows
    public void serialize() {
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
    }

    @Test
    @SneakyThrows
    public void deserialize() {
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
    }
}
