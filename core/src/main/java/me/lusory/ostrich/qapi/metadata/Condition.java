package me.lusory.ostrich.qapi.metadata;

import me.lusory.ostrich.qapi.util.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#configuring-the-schema
public interface Condition {
    static DefaultCondition of(@NotNull String value) {
        return new DefaultCondition(value);
    }

    static NotCondition not(@NotNull String value) {
        return new NotCondition(value);
    }

    static AllCondition all(@NotNull List<Condition> conditions) {
        return new AllCondition(conditions);
    }

    static AnyCondition any(@NotNull List<Condition> conditions) {
        return new AnyCondition(conditions);
    }

    static Condition parse(@NotNull String compact) {
        Preconditions.assertNotNull(compact, "compact must not be null");

        final Function<String, List<String>> conditionListParser = value -> {
            final List<String> conditions = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
            boolean countingNested = false;

            for (final char c : value.toCharArray()) {
                if (countingNested) {
                    builder.append(c);
                    if (c == ']') {
                        countingNested = false;
                    }
                    continue;
                }
                if (c == '[') {
                    countingNested = true;
                } else if (c == ',') {
                    conditions.add(builder.toString());
                    builder.setLength(0);
                    continue;
                }
                builder.append(c);
            }
            return conditions;
        };

        final String type = compact.substring(0, compact.indexOf(':')).toLowerCase(Locale.ROOT);
        final String value = compact.substring(compact.indexOf(':') + 1);
        switch (type) {
            case "default":
                return new DefaultCondition(value);
            case "not":
                return new NotCondition(value);
            case "all":
                return new AllCondition(Collections.unmodifiableList(
                        conditionListParser.apply(value).stream()
                                .map(Condition::parse)
                                .collect(Collectors.toList())
                ));
            case "any":
                return new AnyCondition(Collections.unmodifiableList(
                        conditionListParser.apply(value).stream()
                                .map(Condition::parse)
                                .collect(Collectors.toList())
                ));
            default:
                throw new IllegalArgumentException("Unknown condition type " + type);
        }
    }
}
