package com.ubertob.unlearnoop.zettai.ui

import com.ubertob.unlearnoop.zettai.fp.*


data class TemplateError(override val msg: String) : OutcomeError

typealias TemplateOutcome = Outcome<TemplateError, String>

val tagRegex = """\{(.*?)}""".toRegex()

fun renderTemplate(template: String, data: TagMap): TemplateOutcome =
    data.entries.foldOutcome(template) { text, (k, v) ->
        text.replaceTag(v, k)
    }.checkUnchanged()

private fun TemplateOutcome.checkUnchanged(): TemplateOutcome =
    failIf(tagRegex::containsMatchIn) {
        TemplateError("Mappings missing for tags: ${findAllTags(it)}")
    }

private fun findAllTags(it: String) = tagRegex.findAll(it).joinToString { it.value }


private fun String.replaceTag(tag: ZettaiTag, key: String): TemplateOutcome =
    when (tag) {
        is StringTag -> replace("{${key}}", tag.text).asSuccess()
        is ListTag -> replaceListTag(key, replaceMulti(tag.tagMaps))
    }

private fun String.replaceListTag(key: String, replaceText: (String) -> TemplateOutcome): TemplateOutcome =
    transformReplace(key.toTagRegex()) {
        replaceText(it.value.stripTags(key))
    }

fun String.transformReplace(regex: Regex, expandTags: (MatchResult) -> TemplateOutcome): TemplateOutcome =
    regex.findAll(this).toList()
        .foldOutcome(this) { text, matchResult ->
            expandTags(matchResult)
                .transform {
                    text.replaceRange(matchResult.range.offset(text.length - length), it)
                }
        }

fun IntRange.offset(offset: Int): IntRange =
    IntRange(start + offset, endInclusive + offset)


private fun replaceMulti(tagMaps: List<TagMap>): (String) -> TemplateOutcome =
    { text ->
        tagMaps.map { tagMap ->
            renderTemplate(text, tagMap)
        }.extractList().transform { it.joinToString(separator = "\n") }
    }

private fun String.toTagRegex() = """\{${this}}(.*?)\{/${this}}""".toRegex(RegexOption.DOT_MATCHES_ALL)

private fun String.stripTags(tagName: String): String =
    substring(tagName.length + 2, length - tagName.length - 3)

fun renderTemplatefromResources(fileName: String, data: TagMap): Outcome<TemplateError, String> =
    ZettaiTag::class.java.getResource(fileName).readText().let { renderTemplate(it, data) }

typealias TagMap = Map<String, ZettaiTag>

sealed class ZettaiTag
data class StringTag(val text: String) : ZettaiTag()
data class ListTag(val tagMaps: List<TagMap>) : ZettaiTag()


infix fun String.tag(value: String): Pair<String, ZettaiTag> {
    return this to StringTag(value)
}

infix fun String.tag(value: List<TagMap>): Pair<String, ZettaiTag> {
    return this to ListTag(value)
}