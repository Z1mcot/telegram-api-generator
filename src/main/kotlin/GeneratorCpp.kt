val BASIC_JSON_TYPES_IN_CPP: Set<String> = setOf(
    "std::int64_t",
    "bool",
    "std::string",
    "double",
    "long long"
)

val BASIC_TG_TYPES: Set<String> = setOf(
    "UserId",
    "ChatId",
    "MessageId",
    "BusinessConnectionId",
    "MessageThreadId",
    "MessageEffectId"
)

fun List<DocSection>.toCppModels() = buildString {
    append(generateIncludes())
    appendLine("namespace tgbot {")
    appendLine("using json = nlohmann::json;")
    appendLine("struct TelegramModel { virtual json to_json() const = 0; virtual ~TelegramModel(); };")
    appendLine()
    val forwardDeclarations = buildForwardDeclarations()
    if (forwardDeclarations.isNotEmpty()) {
        appendLine(comment("--- Forward Declarations ---", prefix = "    "))
        forwardDeclarations.forEach {
            appendLine("    $it")
        }
    }
    append(generateValueClasses())
    appendLine()

    appendLine(comment("--- Super Types (Polymorphic Base Classes) ---", prefix = "    "))
    TelegramType.allSuper.forEach { superType ->
        val parentSuperType = TelegramType.from(superType.name).superType
        val parentName = when (parentSuperType) {
            null -> "TelegramModel"
            else -> parentSuperType.name
        }
        appendLine("    struct ${superType.name} : public $parentName {")
        appendLine("        virtual ~${superType.name}() = default;")
        appendLine()
        appendLine("        static std::shared_ptr<${superType.name}> fromJson(const nlohmann::json& j);")
        appendLine("    };")
        appendLine()
    }

    val allType = this@toCppModels.flatMap { section ->
        section.docTypes.map { TelegramType.from(it.name) }
    }
    
    val sortedDocTypes = sortedDocTypes()
    if (sortedDocTypes.isNotEmpty()) {
        appendLine(comment("--- Parameters & Responses ---", prefix = "    "))
        var currentSection: String? = null
        sortedDocTypes.forEach { docTypeWithSection ->
            val sectionName = docTypeWithSection.sectionName
            if (sectionName != null && sectionName != currentSection) {
                appendLine(comment(sectionName, prefix = "    "))
                currentSection = sectionName
            }
            val docType = docTypeWithSection.docType
            appendLine(withIndent(docType.toCppDoc(), "    "))
            appendLine(withIndent(docType.toCppStruct(), "    "))
            appendLine()
        }
    }
    appendLine(comment("--- Requests ---", prefix = "    "))
    this@toCppModels.forEach { section ->
        if (section.docMethods.isNotEmpty()) {
            appendLine(comment(section.name, prefix = "    "))
            section.docMethods.forEach { method ->
                if (method.docParameters.isNotEmpty()) {
                    appendLine(withIndent(method.toCppDoc(showReturn = false), "    "))
                    appendLine(withIndent(method.toCppStruct(), "    "))
                    appendLine()
                }
            }
        }
    }

    appendLine(comment("--- Super Types Serialization ---", prefix = "    "))

    TelegramType.allSuper.forEach { superType ->
        appendLine()
        val allSubtype = allType.filter { it.superType?.name == superType.name }
        // Generate polymorphic JSON serialization using std::variant
        if (allSubtype.isNotEmpty()) {
            appendLine("    std::shared_ptr<${superType.name}> ${superType.name}::fromJson(const json& j) {")

            when (superType) {
                is TelegramType.Super -> {
                    if (superType.deserializer.isEmpty()) {
                        // Try all subtypes until one succeeds
                        allSubtype.forEachIndexed { index, subtype ->
                            if (index == 0) {
                                appendLine("        try {")
                            } else {
                                appendLine("        } catch (...) {")
                                appendLine("            try {")
                            }
                            appendLine("            ${subtype.name} value;")
                            appendLine("            from_json(j, value);")
                            appendLine("            return value;")
                            if (index != 0) {
                                appendLine("            }")
                            }
                        }
                        if (allSubtype.size > 1) {
                            appendLine("        } catch (...) {")
                        }
                        if (allSubtype.size > 1) {
                            appendLine("        }")
                        }
                        appendLine("        throw std::runtime_error(\"Failed to deserialize ${superType.name}\");")
                    } else {
                        // Has custom deserializer - use type field
                        appendLine("        ${superType.deserializerCpp}")
                    }
                }
                else -> {
                    appendLine("        // Not a Super type")
                    appendLine("        (void)j;")
                    appendLine("        (void)value;")
                }
            }
            appendLine("    }")
            appendLine()
        }
    }
    appendLine("} // namespace tgbot")
}

private fun generateIncludes() = buildString {
    appendLine("// Auto-generated Telegram Bot API models for C++")
    appendLine("// Generated by telegram-api-generator. Do not edit manually.\n")
    appendLine("#pragma once")
    appendLine("#include <cstdint>")
    appendLine("#include <optional>")
    appendLine("#include <string>")
    appendLine("#include <vector>")
    appendLine("#include <memory>")
    appendLine("#include <nlohmann/json.hpp>\n")
}

// TODO: either remove or add to telegramtype and build the same
private fun generateValueClasses() = buildString {
    appendLine(comment("--- Value Classes ---", prefix = "    "))
    appendLine("    // chat_id")
    appendLine("    struct ChatId {")
    appendLine("        std::string stringValue;")
    appendLine("        std::int64_t longValue() const { return std::stoll(stringValue); }")
    appendLine()
    appendLine("        json to_json() const { return stringValue; }")
    appendLine("        static void from_json(const json& j, ChatId& value) { value.stringValue = j.get<std::string>(); }")
    appendLine("    };")
    appendLine()
    appendLine("    // user_id")
    appendLine("    struct UserId {")
    appendLine("        std::int64_t longValue;")
    appendLine("        ChatId toChatId() const { return ChatId{std::to_string(longValue)}; }")
    appendLine()
    appendLine("        json to_json() const { return longValue; }")
    appendLine("        static void from_json(const json& j, UserId& value) { value.longValue = j.get<std::int64_t>(); }")
    appendLine("    };")
    appendLine()
    appendLine("    // message_id")
    appendLine("    struct MessageId {")
    appendLine("        std::int64_t longValue;")
    appendLine()
    appendLine("        json to_json() const { return longValue; }")
    appendLine("        static void from_json(const json& j, MessageId& value) { value.longValue = j.get<std::int64_t>(); }")
    appendLine("    };")
    appendLine()
    appendLine("    // business_connection_id")
    appendLine("    struct BusinessConnectionId {")
    appendLine("        std::string stringValue;")
    appendLine()
    appendLine("        json to_json() const { return stringValue; }")
    appendLine("        static void from_json(const json& j, BusinessConnectionId& value) { value.stringValue = j.get<std::string>(); }")
    appendLine("    };")
    appendLine()
    appendLine("    // message_thread_id")
    appendLine("    struct MessageThreadId {")
    appendLine("        std::int64_t longValue;")
    appendLine()
    appendLine("        json to_json() const { return longValue; }")
    appendLine("        static void from_json(const json& j, MessageThreadId& value) { value.longValue = j.get<std::int64_t>(); }")
    appendLine("    };")
    appendLine()
    appendLine("    // message_effect_id")
    appendLine("    struct MessageEffectId {")
    appendLine("        std::string stringValue;")
    appendLine()
    appendLine("        json to_json() const { return stringValue; }")
    appendLine("        static void from_json(const json& j, MessageEffectId& value) { value.stringValue = j.get<std::string>(); }")
    appendLine("    };")
    appendLine("    // parse_mode")
    appendLine("    struct ParseMode {")
    appendLine("        std::string stringValue;")
    appendLine()
    appendLine("        json to_json() const { return stringValue; }")
    appendLine("        static void from_json(const json& j, ParseMode& value) { value.stringValue = j.get<std::string>(); }")
    appendLine("    };")
}

private fun comment(text: String, prefix: String = "") = buildString {
    appendLine()
    appendLine("${prefix}// $text")
}

private fun withIndent(text: String, indent: String) =
    text.lines().joinToString("\n") { line ->
        if (line.isEmpty()) ""
        else indent + line
    }

private fun DocType.toCppDoc() = buildString {
    appendLine("/**")
    appendLine(" * ${description.cleanHtml().replace("\n", "\n * ")}")
    appendLine(" *")
    docFields.forEach {
        appendLine(" * @param ${it.name} ${it.description.cleanHtml()}")
    }
    appendLine(" */")
}

private fun DocMethod.toCppDoc(showReturn: Boolean = true) = buildString {
    appendLine("/**")
    appendLine(" * ${description.cleanHtml().replace("\n", "\n * ")}")
    appendLine(" *")
    docParameters.forEach {
        appendLine(" * @param ${it.name} ${it.description.cleanHtml()}")
    }
    if (showReturn) {
        appendLine(" *")
        appendLine(" * @return ${returns.toCppType()}")
    }
    append(" */")
}

private fun generateVectorSerialization(
    fieldType: String,
    fieldName: String,
    jsonFieldName: String
): String = buildString {
    // Extract element type from std::vector<ElementType>
    if (!fieldType.startsWith("std::vector<")) {
        return@buildString
    }

    val elementType = fieldType.removePrefix("std::vector<").removeSuffix(">")

    val jsonPostfix = when {
        BASIC_JSON_TYPES_IN_CPP.contains(elementType) -> ""
        BASIC_TG_TYPES.contains(elementType) -> ".to_json()"
        else -> "->to_json()"
    }

    if (BASIC_JSON_TYPES_IN_CPP.contains(elementType)) {
        // Direct assignment, no conversion needed
        appendLine("        j[\"$jsonFieldName\"] = $fieldName;")
    } else {
        // Complex type - call to_json on each element
        val tempVarName = "${fieldName}_values"
        appendLine("        std::vector<json> $tempVarName;")
        appendLine("        $tempVarName.reserve($fieldName.size());")
        appendLine("        for (auto& e : $fieldName) {")
        appendLine("            $tempVarName.push_back(e$jsonPostfix);")
        appendLine("        }")
        appendLine("        j[\"$jsonFieldName\"] = $tempVarName;")
    }
}

private fun DocType.toCppStruct() = buildString {
    val telegramType = TelegramType.from(name)
    val superType = telegramType.superType
    val superTypeName = when (superType) {
        null -> "TelegramModel"
        else -> superType.name
    }

    val explicitlySerializedFields: MutableSet<String> = mutableSetOf()
    val vectors: MutableSet<String> = mutableSetOf()
    val ptrs: MutableSet<String> = mutableSetOf()

    appendLine("struct $name : public $superTypeName {")
    appendLine("    virtual ~$name() = default;")
    if (docFields.isEmpty()) {
        appendLine("    // Empty struct")
    } else {
        docFields.forEachIndexed { index, field ->
            val fieldName = field.cppFieldName()
            val comment = field.description.cleanHtml().takeIf { it.isNotBlank() }
            if (comment != null) {
                appendLine("    // ${comment.replace("\n", " ")}")
            }

            val fieldType = field.toCppFieldType(name)
            if (fieldType.contains("std::vector")) {
                vectors.add(fieldName)
            }
            
            if (!BASIC_JSON_TYPES_IN_CPP.contains(fieldType)) {
                explicitlySerializedFields.add(fieldName)
            }

            appendLine("    ${fieldType} $fieldName;")

            if (!BASIC_JSON_TYPES_IN_CPP.contains(fieldType) &&
                !BASIC_TG_TYPES.contains(fieldType) &&
                !vectors.contains(fieldName)
            ) {
                ptrs.add(fieldName)
            }

            if (index != docFields.lastIndex) appendLine()
        }
    }
    appendLine()
    appendLine("    json to_json() const override {")
    appendLine("        json j;")
    if (docFields.isEmpty()) {
        appendLine("        j = json::object();")
    } else {
        docFields.forEach { field ->
            val fieldName = field.cppFieldName()
            val jsonFieldName = field.name
            val fieldType = field.toCppFieldType(name)
            
            val toJsonPostfix = when {
                ptrs.contains(fieldName) && explicitlySerializedFields.contains(fieldName) -> "->to_json()"
                explicitlySerializedFields.contains(fieldName) -> ".to_json()"
                else -> ""
            }

            if (vectors.contains(fieldName)) {
                append(generateVectorSerialization(fieldType, fieldName, jsonFieldName))
            } else {
                appendLine("        j[\"$jsonFieldName\"] = $fieldName$toJsonPostfix;")
            }
        }
    }
    appendLine("        return j.dump();")
    appendLine("    }")
    append("};")
}

private fun DocMethod.toCppStruct() = buildString {
    val structName = requestStructName()
    appendLine("struct $structName {")
    if (docParameters.isEmpty()) {
        appendLine("    // Empty struct")
    } else {
        docParameters.forEachIndexed { index, parameter ->
            val fieldName = parameter.cppFieldName()
            val comment = parameter.description.cleanHtml().takeIf { it.isNotBlank() }
            if (comment != null) {
                appendLine("    // ${comment.replace("\n", " ")}")
            }
            appendLine("    ${parameter.toCppFieldType(name)} $fieldName;")
            if (index != docParameters.lastIndex) appendLine()
        }
    }
    append("};")
}

private fun DocField.cppFieldName() = if (name == "type") "type_" else name

private fun DocParameter.cppFieldName() = if (name == "type") "type_" else name

private fun DocField.toCppFieldType(className: String?) =
    type.toCppTypeWithValueClasses(className, name)

private fun DocParameter.toCppFieldType(className: String?) =
    type.toCppTypeWithValueClasses(className, name)

private fun TelegramType.toCppTypeWithValueClasses(
    className: String?,
    propertyName: String?
): String {
    return when (this) {
        is TelegramType.ListType<*> -> "std::vector<${elementType.toCppTypeWithValueClasses(className, propertyName)}>"
        else -> when {
            propertyName == null -> toCppTypeNoValueClasses()
            className == "User" && propertyName == "id" -> "UserId"
            propertyName == "user_id" -> "UserId"
            className == "Chat" && propertyName == "id" -> "ChatId"
            className == "ChatFullInfo" && propertyName == "id" -> "ChatId"
            propertyName == "chat_id" -> "ChatId"
            propertyName.endsWith("_chat_id") -> "ChatId"
            propertyName == "message_id" -> "MessageId"
            propertyName == "message_ids" -> "MessageId"
            className == "BusinessConnection" && propertyName == "id" -> "BusinessConnectionId"
            propertyName == "business_connection_id" -> "BusinessConnectionId"
            propertyName == "message_thread_id" -> "MessageThreadId"
            propertyName == "message_effect_id" -> "MessageEffectId"
            propertyName == "parse_mode" -> "ParseMode"
            else -> toCppTypeNoValueClasses()
        }
    }
}

private fun TelegramType.toCppType(): String = toCppTypeNoValueClasses()

private fun TelegramType.toCppTypeNoValueClasses(): String = when (this) {
    is TelegramType.Declared -> "std::shared_ptr<$name>"
    is TelegramType.ListType<*> -> "std::vector<${elementType.toCppTypeNoValueClasses()}>"

    TelegramType.Integer -> "std::int64_t"
    TelegramType.StringType -> "std::string"
    TelegramType.Boolean -> "bool"
    TelegramType.Float -> "double"

    TelegramType.CallbackGame,
    TelegramType.InputFile,
    TelegramType.ForumTopicClosed,
    TelegramType.ForumTopicReopened,
    TelegramType.GeneralForumTopicHidden,
    TelegramType.GeneralForumTopicUnhidden,
    TelegramType.GiveawayCreated,
    TelegramType.ParseMode,
    TelegramType.VoiceChatStarted,
    TelegramType.VideoChatStarted -> "std::shared_ptr<$name>"

    is TelegramType.Super -> "std::shared_ptr<$name>"

    is TelegramType.WithAlternative -> when (this) {
        TelegramType.WithAlternative.InputFileOrString -> "std::string"
        TelegramType.WithAlternative.IntegerOrString -> "std::string"
    }
}

fun List<DocSection>.toCppClient() = buildString {
    appendLine("// Auto-generated Telegram Bot API client for C++")
    appendLine("// Generated by telegram-api-generator. Do not edit manually.\n")
    appendLine("#pragma once")
    appendLine("#include \"TelegramModels.hpp\"")
    appendLine("#include <boost/beast/core.hpp>")
    appendLine("#include <boost/beast/http.hpp>")
    appendLine("#include <boost/beast/ssl.hpp>")
    appendLine("#include <boost/beast/version.hpp>")
    appendLine("#include <boost/asio/connect.hpp>")
    appendLine("#include <boost/asio/ip/tcp.hpp>")
    appendLine("#include <boost/asio/ssl/stream.hpp>")
    appendLine("#include <nlohmann/json.hpp>")
    appendLine("#include <iostream>")
    appendLine("#include <string>\n")
    appendLine("namespace telegram {")
    appendLine()
    appendLine("using json = nlohmann::json;")
    appendLine()
    appendLine("template<typename T>")
    appendLine("struct TelegramResponse {")
    appendLine("    bool ok;")
    appendLine("    std::optional<T> result;")
    appendLine("};")
    appendLine()
    appendLine("class TelegramClient {")
    appendLine("private:")
    appendLine("    std::string api_key_;")
    appendLine("    boost::asio::io_context ioc_;")
    appendLine("    boost::asio::ssl::context ctx_{boost::asio::ssl::context::tlsv12_client};")
    appendLine()
    appendLine("    std::string makeRequest(const std::string& method, const std::string& body = \"\") {")
    appendLine("        namespace beast = boost::beast;")
    appendLine("        namespace http = beast::http;")
    appendLine("        namespace net = boost::asio;")
    appendLine("        using tcp = boost::asio::ip::tcp;")
    appendLine()
    appendLine("        try {")
    appendLine("            tcp::resolver resolver{ioc_};")
    appendLine("            beast::ssl_stream<beast::tcp_stream> stream{ioc_, ctx_};")
    appendLine()
    appendLine("            auto const results = resolver.resolve(\"api.telegram.org\", \"443\");")
    appendLine("            beast::get_lowest_layer(stream).connect(results);")
    appendLine("            stream.handshake(boost::asio::ssl::stream_base::client);")
    appendLine()
    appendLine("            std::string target = \"/bot\" + api_key_ + \"/\" + method;")
    appendLine("            http::request<http::string_body> req{http::verb::post, target, 11};")
    appendLine("            req.set(http::field::host, \"api.telegram.org\");")
    appendLine("            req.set(http::field::user_agent, BOOST_BEAST_VERSION_STRING);")
    appendLine("            req.set(http::field::content_type, \"application/json\");")
    appendLine("            req.body() = body.empty() ? \"{}\" : body;")
    appendLine("            req.prepare_payload();")
    appendLine()
    appendLine("            http::write(stream, req);")
    appendLine()
    appendLine("            beast::flat_buffer buffer;")
    appendLine("            http::response<http::dynamic_body> res;")
    appendLine("            http::read(stream, buffer, res);")
    appendLine()
    appendLine("            beast::error_code ec;")
    appendLine("            stream.shutdown(ec);")
    appendLine()
    appendLine("            return beast::buffers_to_string(res.body().data());")
    appendLine("        } catch (std::exception const& e) {")
    appendLine("            throw std::runtime_error(\"Request failed: \" + std::string(e.what()));")
    appendLine("        }")
    appendLine("    }")
    appendLine()
    appendLine("    template<typename T>")
    appendLine("    TelegramResponse<T> parseResponse(const std::string& response_str) {")
    appendLine("        auto j = json::parse(response_str);")
    appendLine("        TelegramResponse<T> result;")
    appendLine("        result.ok = j[\"ok\"].get<bool>();")
    appendLine("        if (result.ok && j.contains(\"result\")) {")
    appendLine("            result.result = j[\"result\"].get<T>();")
    appendLine("        }")
    appendLine("        return result;")
    appendLine("    }")
    appendLine()
    appendLine("public:")
    appendLine("    explicit TelegramClient(const std::string& api_key)")
    appendLine("        : api_key_(api_key) {")
    appendLine("        ctx_.set_default_verify_paths();")
    appendLine("    }")

    this@toCppClient.forEach { section ->
        if (section.docMethods.isNotEmpty()) {
            appendLine(comment(section.name, prefix = "    "))
            section.docMethods.forEach { method ->
                appendLine(withIndent(method.toCppClientMethod(), "    "))
                appendLine()
            }
        }
    }
    appendLine("};")
    appendLine()
    appendLine("} // namespace telegram")
}

private fun DocMethod.toCppClientMethod() = buildString {
    val structName = requestStructName()
    val returnType = returns.toCppType()
    val returnTypeWithVector = when (returns) {
        is TelegramType.ListType<*> -> "std::vector<${returns.elementType.toCppType()}>"
        else -> returnType
    }
    
    appendLine(toCppDoc(showReturn = true))
    if (docParameters.isEmpty()) {
        append("TelegramResponse<$returnTypeWithVector> $name() {")
        appendLine()
        appendLine("    std::string response = makeRequest(\"$name\");")
        appendLine("    return parseResponse<$returnTypeWithVector>(response);")
        append("}")
    } else {
        append("TelegramResponse<$returnTypeWithVector> $name(")
        docParameters.forEachIndexed { index, parameter ->
            val paramType = parameter.toCppFieldType(null)
            val paramName = parameter.cppFieldName()
            if (index == docParameters.lastIndex) {
                appendLine("    $paramType $paramName")
            } else {
                appendLine("    $paramType $paramName,")
            }
        }
        appendLine(") {")
        appendLine("    json j;")
        docParameters.forEach { parameter ->
            val fieldName = parameter.cppFieldName()
            val jsonFieldName = parameter.name
            appendLine("    j[\"$jsonFieldName\"] = $fieldName;")
        }
        appendLine("    std::string body = j.dump();")
        appendLine("    std::string response = makeRequest(\"$name\", body);")
        appendLine("    return parseResponse<$returnTypeWithVector>(response);")
        append("}")
    }
}

private fun DocMethod.requestStructName() =
    name.replaceFirstChar { it.uppercaseChar() } + "Request"

private fun List<DocSection>.buildForwardDeclarations(): List<String> {
    val typeDeclarations = flatMap { section ->
        section.docTypes.map { docType ->
            "struct ${docType.name};"
        }
    }
    val requestDeclarations = flatMap { section ->
        section.docMethods
            .filter { it.docParameters.isNotEmpty() }
            .map { method ->
                "struct ${method.requestStructName()};"
            }
    }
    val superTypeDeclarations = TelegramType.allSuper.map { superType ->
        "struct ${superType.name};"
    }
    return (typeDeclarations + requestDeclarations + superTypeDeclarations).distinct()
}

private data class DocTypeWithSection(
    val docType: DocType,
    val sectionName: String?
)

private fun List<DocSection>.sortedDocTypes(): List<DocTypeWithSection> {
    val docTypesWithSection = flatMap { section ->
        section.docTypes.map { docType -> docType to section.name }
    }
    if (docTypesWithSection.isEmpty()) return emptyList()

    val docTypeMap = docTypesWithSection.associate { (docType, _) -> docType.name to docType }
    val sectionByType = docTypesWithSection.associate { (docType, sectionName) -> docType.name to sectionName }
    val dependencies = docTypeMap.mapValues { (_, docType) ->
        docType.dependencies(docTypeMap.keys)
    }

    val visited = mutableSetOf<String>()
    val visiting = mutableSetOf<String>()
    val order = mutableListOf<String>()

    fun dfs(name: String) {
        if (name !in docTypeMap) return
        if (name in visited) return
        if (!visiting.add(name)) return // cycle detected, keep original insertion order later

        dependencies[name]?.forEach { dependency ->
            dfs(dependency)
        }

        visiting.remove(name)
        visited.add(name)
        order.add(name)
    }

    docTypeMap.keys.forEach { dfs(it) }
    val remaining = docTypeMap.keys.filterNot { it in order.toSet() }
    order.addAll(remaining)

    return order.map { name ->
        DocTypeWithSection(
            docType = docTypeMap.getValue(name),
            sectionName = sectionByType[name]
        )
    }
}

private fun DocType.dependencies(docTypeNames: Set<String>): Set<String> =
    docFields.flatMap { field ->
        field.type.collectDocTypeNames(docTypeNames)
    }.toSet()

private fun TelegramType.collectDocTypeNames(docTypeNames: Set<String>): List<String> = when (this) {
    is TelegramType.Declared -> listOfNotNull(name.takeIf { it in docTypeNames })
    is TelegramType.ListType<*> -> elementType.collectDocTypeNames(docTypeNames)
    is TelegramType.Super -> listOfNotNull(name.takeIf { it in docTypeNames })
    is TelegramType.WithAlternative -> emptyList()
    else -> emptyList()
}

private fun String.cleanHtml(): String {
    return this
        .replace(Regex("<[^>]+>"), "") // Remove HTML tags
        .replace("&nbsp;", " ")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .trim()
}


