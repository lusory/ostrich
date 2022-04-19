package me.lusory.ostrich.gen

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.squareup.javapoet.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import javax.lang.model.element.Modifier

class Generator(files: List<File>, private val sourceDir: File) {
    companion object {
        private val KEYWORDS: Set<String> = setOf(
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
        )
        private val TYPE_REF_REGEX = Regex("@([a-zA-Z]+)")

        private val NULLABLE: AnnotationSpec = AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "Nullable")).build()
        private val UNMODIFIABLE: AnnotationSpec = AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "Unmodifiable")).build()

        private val MAPPER: ObjectMapper = jsonMapper {
            addModule(kotlinModule())
            enable(JsonParser.Feature.ALLOW_YAML_COMMENTS, JsonParser.Feature.ALLOW_SINGLE_QUOTES)
        }

        private val logger: Logger = LoggerFactory.getLogger(Generator::class.java)
    }

    val readers: MutableMap<String, ReaderItem> = mutableMapOf()

    init {
        for (file: File in files) {
            val split: List<String> = file.readText().split("\r\n", "\n")
            val descriptors: MutableList<Pair<String, String>> = mutableListOf()

            var skipTimes = 0
            var iterating = false
            var name: String? = null
            val builder = StringBuilder()
            split.forEachIndexed { i, item ->
                if (skipTimes > 0) {
                    skipTimes--
                    return@forEachIndexed
                }

                if (iterating) {
                    if (item.startsWith("##")) {
                        iterating = false
                        descriptors.add(Pair(
                            name ?: throw IllegalStateException(),
                            builder.toString()
                        ))
                        builder.setLength(0)
                    } else if (item == "#" || item.isEmpty()) {
                        builder.append("\n")
                    } else {
                        builder.append(item.substring(if (item.startsWith("# ")) 2 else 1)).append("\n")
                    }
                    return@forEachIndexed
                }

                if (item.startsWith("# ")) {
                    return@forEachIndexed
                }

                // element doc
                if (item.startsWith("##") && split[i + 1].startsWith("# @")) {
                    skipTimes += 1
                    if (split[i + 2].trim() == "#" || split[i + 2].trim().isEmpty()) {
                        skipTimes += 1
                    }
                    name = split[i + 1].substringAfterLast('@').dropLast(1)
                    iterating = true
                }
            }

            descriptors.forEach { readers[it.first] = ReaderItem(file.nameWithoutExtension, it.first, it.second) }

            MAPPER.readValues(MAPPER.createParser(file), JsonNode::class.java)
                .forEach { node ->
                    readers[node.schemaName ?: return@forEach]?.let { item ->
                        item.json = node
                        item.schemaType = node.schemaType
                        item.schemaName = node.get(item.schemaType).asText()
                    }
                }
        }
    }

    fun generate() {
        for (item: ReaderItem in readers.values) {
            val className: ClassName = ClassName.get("me.lusory.ostrich.qapi.${item.file.replaceReservedKeywords()}", item.schemaName)

            var builder: TypeSpec.Builder
            if (item.schemaType == "enum") {
                builder = TypeSpec.enumBuilder(className)
                    .addSuperinterface(ClassName.get("me.lusory.ostrich.qapi", "QEnum"))
                    .addField(java.lang.String::class.java, "_if", Modifier.PRIVATE, Modifier.FINAL)
                    .addField(ParameterizedTypeName.get(java.util.List::class.java, java.lang.String::class.java), "features", Modifier.PRIVATE, Modifier.FINAL)
                    .addMethod(
                        MethodSpec.constructorBuilder()
                            .addParameter(java.lang.String::class.java, "_if")
                            .addParameter(ArrayTypeName.of(java.lang.String::class.java), "features")
                            .varargs()
                            .addStatement("this._if = _if")
                            .addStatement("this.features = \$T.asList(features)", Arrays::class.java)
                            .build()
                    )
                    .addMethod(
                        MethodSpec.methodBuilder("getIf")
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(Override::class.java)
                            .addAnnotation(NULLABLE)
                            .returns(java.lang.String::class.java)
                            .addStatement("return _if")
                            .build()
                    )
                    .addMethod(
                        MethodSpec.methodBuilder("getFeatures")
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(Override::class.java)
                            .addAnnotation(UNMODIFIABLE)
                            .returns(ParameterizedTypeName.get(java.util.List::class.java, java.lang.String::class.java))
                            .addStatement("return features")
                            .build()
                    )

                item.json!!.get("data").forEach { node ->
                    val textifiedNode: String = if (node.isTextual) node.asText() else node.get("name").asText()
                    val ifAttribute: String? = node.get("if")?.asText()
                    val featuresAttribute: List<String> = node.get("features")?.map(JsonNode::asText) ?: listOf()

                    builder.addEnumConstant(
                        textifiedNode.toUpperCase().replaceReservedKeywords(),
                        TypeSpec.anonymousClassBuilder("\$L", CodeBlock.join(mutableListOf(CodeBlock.of("\$S", ifAttribute)).also { it.addAll(featuresAttribute.map { e -> CodeBlock.of("\$S", e) }) }, ", "))
                            .addMethod(
                                MethodSpec.methodBuilder("toString")
                                    .addAnnotation(Override::class.java)
                                    .addModifiers(Modifier.PUBLIC)
                                    .addStatement("return \$S", textifiedNode)
                                    .returns(String::class.java)
                                    .build()
                            )
                            .build()
                    )
                }
            } else {
                continue
            }

            builder.addModifiers(Modifier.PUBLIC)
                .addJavadoc(
                    item.doc.replace("$", "$$")
                        .split("\n")
                        .joinToString("\n") { l -> if (l.startsWith('@')) l.drop(1) else l }
                        .replace(TYPE_REF_REGEX) { r ->
                            readers[r.groupValues[1]]
                                ?.let { i -> if (isDataHolder(i.schemaType!!)) i else null }
                                ?.let { i -> "{@link me.lusory.ostrich.qapi.${i.file.replaceReservedKeywords()}.${i.name}}" }
                                ?: r.groupValues[1]
                        }
                        .replace("\n", "<br>\n") // add line break for better view
                )

            JavaFile.builder(className.packageName(), builder.build())
                .indent("    ") // 4 space indent
                .skipJavaLangImports(true)
                .build()
                .writeTo(sourceDir)

            logger.info("Wrote [].[].", className.packageName(), className.simpleName())
        }
    }

    private fun String.replaceReservedKeywords(): String = replace('-', '_').let { s ->
        if (KEYWORDS.any { it == s }) "_$s" else s
    }

    private fun isDataHolder(s: String): Boolean = s == "union" || s == "enum" || s == "type" || s == "struct"

    private val JsonNode.schemaType: String
        get() = when {
            has("command") -> "command"
            has("struct") -> "struct"
            has("enum") -> "enum"
            has("union") -> "union"
            has("alternate") -> "alternate"
            has("event") -> "event"
            has("include") -> "include"
            has("pragma") -> "pragma"
            else -> throw IllegalArgumentException("Unknown schema type")
        }

    private val JsonNode.schemaName: String?
        get() = when (schemaType) {
            "pragma" -> null
            "include" -> null
            else -> get(schemaType)?.asText()
        }
}