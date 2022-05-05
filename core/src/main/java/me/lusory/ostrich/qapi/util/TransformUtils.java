package me.lusory.ostrich.qapi.util;

import lombok.experimental.UtilityClass;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class TransformUtils {
    public List<String> RESERVED_KEYWORDS = Arrays.asList(
            "_",
            "abstract",
            "assert",
            "boolean",
            "break",
            "byte",
            "case",
            "catch",
            "char",
            "class",
            "const",
            "continue",
            "default",
            "do",
            "double",
            "else",
            "enum",
            "extends",
            "false",
            "final",
            "finally",
            "float",
            "for",
            "goto",
            "if",
            "implements",
            "import",
            "instanceof",
            "int",
            "interface",
            "Iterable",
            "long",
            "native",
            "new",
            "null",
            "Object",
            "package",
            "private",
            "protected",
            "public",
            "return",
            "RuntimeException",
            "short",
            "static",
            "static final",
            "strictfp",
            "super",
            "switch",
            "synchronized",
            "this",
            "throw",
            "throws",
            "transient",
            "true",
            "try",
            "undefined",
            "var",
            "void",
            "volatile",
            "while"
    );

    private final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    public String replaceReservedKeywords(String s) {
        final String replaced = s.replace('-', '_');

        if (RESERVED_KEYWORDS.contains(replaced)) {
            return "_" + replaced;
        }
        try {
            // sanitize numbers
            NUMBER_FORMAT.parse(replaced);
            return "_" + replaced;
        } catch (ParseException ignored) {
            // ignored
        }
        return replaced;
    }
}
