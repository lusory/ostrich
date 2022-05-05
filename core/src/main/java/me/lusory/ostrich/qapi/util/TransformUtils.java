package me.lusory.ostrich.qapi.util;

import lombok.experimental.UtilityClass;

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

    public String replaceReservedKeywords(String s) {
        final String replaced = s.replace('-', '_');

        if (RESERVED_KEYWORDS.contains(replaced)) {
            return "_" + replaced;
        }
        return replaced;
    }
}
