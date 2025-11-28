sealed class TelegramType(val name: String, val superType: TelegramType? = findSuper(name)) {

    class Declared(docName: String, superType: TelegramType? = findSuper(docName)) : TelegramType(docName, superType)

    class ListType<T : TelegramType>(val elementType: T) : TelegramType("List<$elementType>", superType = null)

    object Integer : TelegramType("Integer", superType = null)
    object StringType : TelegramType("String", superType = null)
    object Boolean : TelegramType("Boolean", superType = null)
    object Float : TelegramType("Float", superType = null)
    object CallbackGame : TelegramType("CallbackGame", superType = null)
    object InputFile : TelegramType("InputFile", superType = null)
    object ParseMode : TelegramType("ParseMode", superType = null)
    object VoiceChatStarted : TelegramType("VoiceChatStarted", superType = null)
    object VideoChatStarted : TelegramType("VideoChatStarted", superType = null)
    object ForumTopicClosed : TelegramType("ForumTopicClosed", superType = null)
    object ForumTopicReopened : TelegramType("ForumTopicReopened", superType = null)
    object GeneralForumTopicHidden : TelegramType("GeneralForumTopicHidden", superType = null)
    object GeneralForumTopicUnhidden : TelegramType("GeneralForumTopicUnhidden", superType = null)
    object GiveawayCreated : TelegramType("GiveawayCreated", superType = null)

    sealed class Super(
        name: String,
        val subclasses: (String) -> kotlin.Boolean,
        val deserializer: String,
        val deserializerCpp: String,
    ) : TelegramType(name, superType = null) {

        object InputMessageContent : Super(
            name = "InputMessageContent",
            subclasses = { it.startsWith("Input") && it.endsWith("MessageContent") },
            deserializer = "",
            deserializerCpp = ""
        )

        object InlineQueryResult : Super(
            name = "InlineQueryResult",
            subclasses = { it.startsWith("InlineQueryResult") && "Results" !in it },
            deserializer = "",
            deserializerCpp = ""
        )

        object PassportElementError : Super(
            name = "PassportElementError",
            subclasses = { it.startsWith("PassportElementError") },
            deserializer = "",
            deserializerCpp = ""
        )

        object InputMedia : Super(
            name = "InputMedia",
            subclasses = { it.startsWith("InputMedia") },
            deserializer = """
            when (val type = jsonElement.jsonObject.getValue("type").jsonPrimitive.content) {
                "photo" -> InputMediaPhoto.serializer()
                "video" -> InputMediaVideo.serializer()
                "animation" -> InputMediaAnimation.serializer()
                "audio" -> InputMediaAudio.serializer()
                "document" -> InputMediaDocument.serializer()
               else -> error("unknown type: " + type)
            }
            """,
            deserializerCpp = """
            std::string type = j.at("type");
            
            if (type == "photo")    return InputMediaPhoto::fromJson(j);
            if (type == "video")    return InputMediaVideo::fromJson(j);
            if (type == "animation")return InputMediaAnimation::fromJson(j);
            if (type == "audio")    return InputMediaAudio::fromJson(j);
            if (type == "document") return InputMediaDocument::fromJson(j);

            throw std::runtime_error("Unknown InputMedia type: " + type);
            """
        )

        object ChatMember : Super(
            name = "ChatMember",
            subclasses = { it.startsWith("ChatMember") },
            deserializer = "",
            deserializerCpp = ""
        )

        object BotCommandScope : Super(
            name = "BotCommandScope",
            subclasses = { it.startsWith("BotCommandScope") },
            deserializer = "",
            deserializerCpp = ""
        )

        object ReactionType : Super(
            name = "ReactionType",
            subclasses = { it.startsWith("ReactionType") },
            deserializer = "",
            deserializerCpp = ""
        )

        object MessageOrigin : Super(
            name = "MessageOrigin",
            subclasses = { it.startsWith("MessageOrigin") },
            deserializer = "",
            deserializerCpp = ""
        )

        object ChatBoostSource : Super(
            name = "ChatBoostSource",
            subclasses = { it.startsWith("ChatBoostSource") },
            deserializer = "",
            deserializerCpp = ""
        )

        object MenuButton : Super(
            name = "MenuButton",
            subclasses = { it.startsWith("MenuButton") },
            deserializer = "",
            deserializerCpp = ""
        )

        object BackgroundFill : Super(
            name = "BackgroundFill",
            subclasses = { it.startsWith("BackgroundFill") },
            deserializer = "",
            deserializerCpp = ""
        )

        object BackgroundType : Super(
            name = "BackgroundType",
            subclasses = { it.startsWith("BackgroundType") },
            deserializer = "",
            deserializerCpp = ""
        )

        object RevenueWithdrawalState : Super(
            name = "RevenueWithdrawalState",
            subclasses = { it.startsWith("RevenueWithdrawalState") },
            deserializer = "",
            deserializerCpp = ""
        )

        object TransactionPartner : Super(
            name = "TransactionPartner",
            subclasses = { it.startsWith("TransactionPartner") },
            deserializer = "",
            deserializerCpp = ""
        )

        object PaidMedia : Super(
            name = "PaidMedia",
            subclasses = { it.startsWith("PaidMedia") },
            deserializer = "",
            deserializerCpp = ""
        )

        object InputPaidMedia : Super(
            name = "InputPaidMedia",
            subclasses = { it.startsWith("InputPaidMedia") },
            deserializer = "",
            deserializerCpp = ""
        )

        object KeyboardOption : Super(
            name = "KeyboardOption",
            subclasses = {
                it in listOf(
                    "InlineKeyboardMarkup",
                    "ReplyKeyboardMarkup",
                    "ReplyKeyboardRemove",
                    "ForceReply"
                )
            },
            deserializer = "",
            deserializerCpp = ""
        )

        object MaybeInaccessibleMessage : Super(
            name = "MaybeInaccessibleMessage",
            subclasses = { it in listOf("Message", "InaccessibleMessage") },
            deserializer = """if (jsonElement.jsonObject.getValue("date").jsonPrimitive.long == 0L) {
                                    InaccessibleMessage.serializer()
                                } else {
                                    Message.serializer()
                                }""",
            deserializerCpp = """
                long date = j.at("date").get<long>();

                if (date == 0) {
                    // соответствие InaccessibleMessage.serializer()
                    return InaccessibleMessage::fromJson(j);
                } else {
                    // соответствие Message.serializer()
                    return Message::fromJson(j);
                }
            """
        )

        object StoryAreaType : Super(
            name = "StoryAreaType",
            subclasses = { it.startsWith("StoryAreaType") },
            deserializer = "",
            deserializerCpp = ""
        )

        object OwnedGift : Super(
            name = "OwnedGift",
            subclasses = { it.startsWith("OwnedGift") && it != "OwnedGifts" },
            deserializer = "",
            deserializerCpp = ""
        )

        object InputProfilePhoto : Super(
            name = "InputProfilePhoto",
            subclasses = { it.startsWith("InputProfilePhoto") },
            deserializer = "",
            deserializerCpp = ""
        )

        object InputStoryContent : Super(
            name = "InputStoryContent",
            subclasses = { it.startsWith("InputStoryContent") },
            deserializer = "",
            deserializerCpp = ""
        )
    }

    sealed class WithAlternative(name: String, val validTypes: List<TelegramType>, superType: TelegramType?) :
        TelegramType(name, superType) {
        object InputFileOrString : WithAlternative(
            name = "InputFileOrString",
            validTypes = listOf(
                InputFile,
                StringType
            ),
            superType = null
        )

        object IntegerOrString : WithAlternative(
            name = "IntegerOrString",
            validTypes = listOf(
                Integer,
                StringType
            ),
            superType = null
        )
    }

    override fun toString() = name

    fun getTypeWithoutGenerics(): TelegramType = if (this is ListType<*>) elementType.getTypeWithoutGenerics() else this

    companion object {
        val allSuper = listOf(
            Super.InputMedia,
            Super.InputMessageContent,
            Super.InlineQueryResult,
            Super.PassportElementError,
            Super.ChatMember,
            Super.BotCommandScope,
            Super.ReactionType,
            Super.MessageOrigin,
            Super.ChatBoostSource,
            Super.MenuButton,
            Super.BackgroundFill,
            Super.BackgroundType,
            Super.RevenueWithdrawalState,
            Super.TransactionPartner,
            Super.PaidMedia,
            Super.InputPaidMedia,
            Super.KeyboardOption,
            Super.MaybeInaccessibleMessage,
            Super.StoryAreaType,
            Super.OwnedGift,
            Super.InputProfilePhoto,
            Super.InputStoryContent,
            WithAlternative.InputFileOrString,
            WithAlternative.IntegerOrString,
        )

        private fun findSuper(docName: String) = allSuper.filterIsInstance(WithAlternative::class.java)
            .firstOrNull { docName in it.validTypes.map { it.name } }
            ?: allSuper.filterIsInstance(Super::class.java)
                .firstOrNull { it.subclasses(docName) }

        fun from(type: String): TelegramType = when (type) {
            "Integer" -> Integer
            "String" -> StringType
            "Boolean" -> Boolean
            "Float" -> Float
            "CallbackGame" -> CallbackGame
            "InputMedia" -> Super.InputMedia
            "InputFile" -> InputFile
            "ParseMode" -> ParseMode
            "VoiceChatStarted" -> VoiceChatStarted
            "VideoChatStarted" -> VideoChatStarted
            "ForumTopicClosed" -> ForumTopicClosed
            "ForumTopicReopened" -> ForumTopicReopened
            "GeneralForumTopicHidden" -> GeneralForumTopicHidden
            "GeneralForumTopicUnhidden" -> GeneralForumTopicUnhidden
            "GiveawayCreated" -> GiveawayCreated
            "InputMessageContent" -> Super.InputMessageContent
            "InlineQueryResult" -> Super.InlineQueryResult
            "ReactionType" -> Super.ReactionType
            "MessageOrigin" -> Super.MessageOrigin
            "ChatBoostSource" -> Super.ChatBoostSource
            "PassportElementError" -> Super.PassportElementError
            "ChatMember" -> Super.ChatMember
            "MenuButton" -> Super.MenuButton
            "BackgroundFill" -> Super.BackgroundFill
            "BackgroundType" -> Super.BackgroundType
            "RevenueWithdrawalState" -> Super.RevenueWithdrawalState
            "TransactionPartner" -> Super.TransactionPartner
            "PaidMedia" -> Super.PaidMedia
            "InputPaidMedia" -> Super.InputPaidMedia
            "BotCommandScope" -> Super.BotCommandScope
            "KeyboardOption" -> Super.KeyboardOption
            "MaybeInaccessibleMessage" -> Super.MaybeInaccessibleMessage
            "StoryAreaType" -> Super.StoryAreaType
            "OwnedGift" -> Super.OwnedGift
            "InputProfilePhoto" -> Super.InputProfilePhoto
            "InputStoryContent" -> Super.InputStoryContent
            "InputFileOrString" -> WithAlternative.InputFileOrString
            "IntegerOrString" -> WithAlternative.IntegerOrString
            else -> {
                if (type.startsWith("Array of ")) {
                    ListType(from(type.removePrefix("Array of ")))
                } else {
                    Declared(type)
                }
            }
        }
    }
}

