package ru.skillbranch.skillarticles.markdown

import android.util.Log
import java.lang.StringBuilder
import java.util.regex.Pattern

object MarkdownParser {

    private val LINE_SEPARATOR = "\n"

    //group regex
    private const val UNORDERED_LIST_ITEM_GROUP = "(^[*+-] .+$)"
    private const val HEADER_GROUP = "(^#{1,6} .+?$)"
    private const val QUOTE_GROUP = "(^> .+?$)"
    private const val ITALIC_GROUP = "((?<!\\*)\\*[^*].*?[^*]?\\*(?!\\*)|(?<!_)_[^_].*?[^_]?_(?!_))"
    private const val BOLD_GROUP ="((?<!\\*)\\*{2}[^*].*?[^*]?\\*{2}(?!\\*)|(?<!_)_{2}[^_].*?[^_]?_{2}(?!_))"
    private const val STRIKE_GROUP = "((?<!~)~{2}[^*].*?[^*]?~{2}(?!~))"
    private const val RULE_GROUP = "(^[-_*]{3}$)"
    private const val INLINE_GROUP = "((?<!`)`[^`\\s].*?[^`\\s]?`(?!`))"
    private const val LINK_GROUP = "(\\[[^\\[\\]]*?]\\(.+?\\)|^\\[*?]\\(.*?\\))"
    private const val BLOCK_CODE_GROUP = "" //TODO implement me
    private const val ORDER_LIST_GROUP = "" //TODO implement me

    //result regex
    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP" +
            "|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP"
    //|$BLOCK_CODE_GROUP|$ORDER_LIST_GROUP optionally

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    /**
     * parse markdown text to elements
     */
    fun parse(string: String): MarkdownText {
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))
        return MarkdownText(elements)
    }

    /**
     * clear markdown text to string without markdown characters
     */
    fun clear(string: String?): String? {
        var count = 0
        if (string!=null) {
            val sb = StringBuilder("")
            var continueNum = 0
            outerloop@
            for (i in string.indices){
                if (continueNum > 0){
                    continueNum--
                    continue
                }

                //если символы '-' поодиночке, то оставляем
                if (i > 0 && i < string.length && string[i - 1].toInt() != 10){
                    val char = string[i]
                    var isSingle = true
                    if (char == '-' || char == '_') {
                        var j = i + 1
                        var k = i - 1
                         while (string[j].toInt() != 10 && j < string.length - 1){
                            if (string[j] == char){
                                isSingle = false
                                break
                            }
                            else j++
                        }
                        while (string[k].toInt() != 10 && k != 1){
                            if (string[k] == char){
                                isSingle = false
                                break
                            }
                            else k--
                        }
                        if (isSingle){
                            sb.append(char)
                            continue
                        }
                    }
                }

                //проверка на ссылки
                if (string[i] == '['){
                    var j = i + 1
                    while (string[j].toInt() != 10 && j < string.length){
                        if (string[j] == ']' && string[j + 1] == '('){
                            var k = j + 1
                            while (string[k].toInt() != 10 && k < string.length){
                                if (string[k] == ')') continue@outerloop
                                else k++
                            }
                        }
                        j++
                    }
                }
                if (string[i] == ']'){
                    var j = i - 1
                    while (string[j].toInt() != 10 && j != 0){
                        if (string[j] == '[' && string[i + 1] == '('){
                            var k = i + 1
                            while (string[k].toInt() != 10 && k < string.length){
                                if (string[k] == ')') {
                                    continueNum = k - i
                                    continue@outerloop
                                }
                                else k++
                            }
                        }
                        j--
                    }
                }


                //проверка на тройные знаки
                if (i < string.length - 3) {
                    //если ___ --- ***
                    if (string[i] == '_' && string[i + 1] == '_' && string[i + 2] == '_' ||
                        string[i] == '-' && string[i + 1] == '-' && string[i + 2] == '-' ||
                        string[i] == '*' && string[i + 1] == '*' && string[i + 2] == '*'){
                        continueNum = 2
                        sb.append(" ")
                        continue
                    }
                    //если ```
                    if (string[i] == '`' && string[i + 1] == '`' && string[i + 2] == '`') {
                        continueNum = 2
                        sb.append("```")
                        continue
                        }
                }

                //если символ #
                if (string[i] == '#' || string[i] == '*' || string[i] == '_' || string[i] == '~' || string[i] == '-'
                    || string[i] == '+' || string[i] == '>') continue
                //
                if (string[i] == ' ' && (string[i - 1] == '#' || string[i - 1] == '+' || string[i - 1] == '-' || string[i - 1] == '#'
                            || string[i - 1] == '>' || string[i - 1] == '#')) continue

                if (string[i] == ' ' && string[i - 1] == '*' && string[i - 2].toInt() == 10) continue

                //для шаблона на пробельный символ
                val str = if (i < string.length - 2) string.subSequence(i + 1, i + 2)
                else if (i == string.length -2) string.last().toString()
                else " "

                //если символ ` и после любой непробельный символ
                if (string[i] == '`' && Pattern.matches("\\S", str)){
                    //то ищем есть ли еще ` до конца строки
                    var j = i + 1
                    while (string[j].toInt() != 10 && j != string.length){
                        //если символ ` и перед любой непробельный символ
                        val str1 = string.subSequence(j - 1, j)
                        val c = string[j].toInt()
                        if (string[j] == '`' && Pattern.matches("\\S", str1)) continue@outerloop
                        else j++
                    }
                }
                //если символ ` и перед любой непробельный символ
                val str2 = if (i > 0) string.subSequence(i - 1, i) else "stub"
                if (string[i] == '`' && Pattern.matches("\\S", str2)){
                    //то ищем есть ли еще ` до начала строки
                    var j = i - 1
                    while (string[j].toInt() != 10 && j != -1){
                        //если символ ` и после любой непробельный символ
                        val str3 = if (j < string.length - 2) string.subSequence(j + 1, j + 2)
                        else string.last().toString()
                        if (string[j] == '`' && Pattern.matches("\\S", str3)) continue@outerloop
                        else j--
                    }
                }
                sb.append(string[i])
                }
            return sb.toString()
            }
        return null
    }

    /**
     * find markdown elements in markdown text
     */
    private fun findElements(string: CharSequence): List<Element> {
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@ while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()
            //if something is found then everything before - TEXT
            if (lastStartIndex < startIndex){
                parents.add(Element.Text(string.subSequence(lastStartIndex, startIndex)))
            }

            //found text
            var text: CharSequence

            //groups range for iterate by groups (1..9) or (1..11) optionally
            val groups = 1..9
            var group = 1
            for (gr in groups){
                if (matcher.group(gr) != null){
                    group = gr
                    break
                }
            }

            when (group) {
                //NOT FOUND -> BREAK
                -1 -> break@loop

                //UNORDERED LIST
                1 -> {
                    //text without "*. "
                    text = string.subSequence(startIndex.plus(2), endIndex)

                    //find inner elements
                    val subs = findElements(text)
                    val element = Element.UnorderedListItem(text, subs)
                    parents.add(element)

                    //next find start from position "endIndex" (last regex character)
                    lastStartIndex = endIndex
                }

                //HEADER
                2 -> {
                    val reg = "^#{1,6}".toRegex().find(string.subSequence(startIndex, endIndex))
                    val level = reg!!.value.length

                    //text without "{#} "
                    text = string.subSequence(startIndex.plus(level.inc()), endIndex)

                    val element = Element.Header(level, text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //QUOTE
                3 -> {
                    //text without "> "
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    val subs = findElements(text)
                    val element = Element.Quote(text, subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //ITALIC
                4 -> {
                    //text without "*{}*"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subs = findElements(text)
                    val element = Element.Italic(text, subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //BOLD
                5 -> {
                    //text without "**{}**"
                    text = string.subSequence(startIndex.plus(2), endIndex.plus(-2))
                    val subs = findElements(text)
                    val element = Element.Bold(text, subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //STRIKE
                6 -> {
                    //text without "~~{}~~"
                    text = string.subSequence(startIndex.plus(2), endIndex.plus(-2))
                    val subs = findElements(text)
                    val element = Element.Strike(text, subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //RULE
                7 -> {
                    //text without "***" insert empty character
                    val element = Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //RULE
                8 -> {
                    //text without "`{}`"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val element = Element.InlineCode(text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //LINK
                9 -> {
                    //full text for regex
                    text = string.subSequence(startIndex, endIndex)
                    val (title:String, link:String)= "\\[(.*)]\\((.*)\\)".toRegex().find(text)!!.destructured
                    val element = Element.Link(link, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                //10 -> BLOCK CODE - optionally
                10 -> {
                    //TODO implement me
                }

                //11 -> NUMERIC LIST
                11 -> {
                    //TODO implement me
                }
            }

        }

        //all other is Text
        if (lastStartIndex < string.length){
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(Element.Text(text))
        }
        return parents
    }
}

data class MarkdownText(val elements: List<Element>)

sealed class Element() {
    abstract val text: CharSequence
    abstract val elements: List<Element>

    data class Text(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class UnorderedListItem(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Header(
        val level: Int = 1,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Quote(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Italic(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Bold(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Strike(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Rule(
        override val text: CharSequence = " ", //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class InlineCode(
        override val text: CharSequence, //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Link(
        val link: String,
        override val text: CharSequence, //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class OrderedListItem(
        val order: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class BlockCode(
        val type: Type = Type.MIDDLE,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element() {
        enum class Type { START, END, MIDDLE, SINGLE }
    }
}