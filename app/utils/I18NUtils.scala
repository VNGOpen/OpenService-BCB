package utils

object I18NUtils {
    def renderMessage(messages: play.i18n.Messages, msgKey: String) = {
        if (!msgKey.contains("||")) msgKey else {
            val tokens = msgKey.split("\\|\\|")
            messages.at(tokens.head, tokens.tail)
        }
    }

    def renderMessage(messages: play.api.i18n.Messages, msgKey: String) = {
        if (!msgKey.contains("||")) msgKey else {
            val tokens = msgKey.split("\\|\\|")
            messages.apply(tokens.head, tokens.tail)
        }
    }
}