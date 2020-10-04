package ru.skillbranch.skillarticles.data.repositories

import java.util.regex.Pattern
import kotlin.text.StringBuilder

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
    private const val BLOCK_CODE_GROUP = "(^```[\\s\\S]+?```$)"
    private const val ORDER_LIST_GROUP = "(^\\d{1,2}\\.\\s.+?$)"
    private const val IMAGE_GROUP = "(^!\\[[^\\[\\]]*?\\]\\(.*?\\)$)"

    //result regex
    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP" +
            "|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP" +
            "|$BLOCK_CODE_GROUP|$ORDER_LIST_GROUP|$IMAGE_GROUP" //optionally

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    /**
     * parse markdown text to elements
     */
    fun parse(string: String): List<MarkdownElement> {
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))
        return elements.fold(mutableListOf()){acc, element ->
            val last = acc.lastOrNull()
            when (element) {
                is Element.Image -> acc.add(
                    MarkdownElement.Image(
                        element,
                        last?.bounds?.second ?: 0
                    )
                )
                is Element.BlockCode -> acc.add(
                    MarkdownElement.Scroll(
                        element,
                        last?.bounds?.second ?: 0
                    )
                )
                else -> {
                    if (last is MarkdownElement.Text) last.elements.add(element)
                    else acc.add(
                        MarkdownElement.Text(
                            mutableListOf(element),
                            last?.bounds?.second ?: 0
                        )
                    )
                }
            }
            acc
        }
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
                parents.add(
                    Element.Text(
                        string.subSequence(lastStartIndex, startIndex)
                    )
                )
            }

            //found text
            var text: CharSequence

            //groups range for iterate by groups (1..9) or (1..11) optionally
            val groups = 1..12
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
                    val subs =
                        findElements(
                            text
                        )
                    val element =
                        Element.UnorderedListItem(
                            text,
                            subs
                        )
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

                    val element =
                        Element.Header(
                            level,
                            text
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //QUOTE
                3 -> {
                    //text without "> "
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    val subs =
                        findElements(
                            text
                        )
                    val element =
                        Element.Quote(
                            text,
                            subs
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //ITALIC
                4 -> {
                    //text without "*{}*"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subs =
                        findElements(
                            text
                        )
                    val element =
                        Element.Italic(
                            text,
                            subs
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //BOLD
                5 -> {
                    //text without "**{}**"
                    text = string.subSequence(startIndex.plus(2), endIndex.plus(-2))
                    val subs =
                        findElements(
                            text
                        )
                    val element =
                        Element.Bold(
                            text,
                            subs
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //STRIKE
                6 -> {
                    //text without "~~{}~~"
                    text = string.subSequence(startIndex.plus(2), endIndex.plus(-2))
                    val subs =
                        findElements(
                            text
                        )
                    val element =
                        Element.Strike(
                            text,
                            subs
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //RULE
                7 -> {
                    //text without "***" insert empty character
                    val element =
                        Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //RULE
                8 -> {
                    //text without "`{}`"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val element =
                        Element.InlineCode(
                            text
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //LINK
                9 -> {
                    //full text for regex
                    text = string.subSequence(startIndex, endIndex)
                    val (title:String, link:String)= "\\[(.*)]\\((.*)\\)".toRegex().find(text)!!.destructured
                    val element =
                        Element.Link(
                            link,
                            title
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                //10 -> BLOCK CODE - optionally
                10 -> {
                    text = string.subSequence(startIndex.plus(3), endIndex.plus(-3)).toString()
                    val element = Element.BlockCode(text)
                    parents.add(element)
/*
                    if (text.contains(LINE_SEPARATOR)){
                        for ((index:Int, line:String) in text.lines().withIndex()){
                            when (index){
                                text.lines().lastIndex -> parents.add(
                                    Element.BlockCode(
                                        Element.BlockCode.Type.END,
                                        line
                                    )
                                )
                                0 -> parents.add(
                                    Element.BlockCode(
                                        Element.BlockCode.Type.START,
                                        line + LINE_SEPARATOR
                                    )
                                )
                                else -> parents.add(
                                    Element.BlockCode(
                                        Element.BlockCode.Type.MIDDLE,
                                        line + LINE_SEPARATOR
                                    )
                                )
                            }
                        }
                    }
                    else parents.add(
                        Element.BlockCode(
                            Element.BlockCode.Type.SINGLE,
                            text
                        )
                    )
*/
                    lastStartIndex = endIndex
                }

                //11 -> NUMERIC LIST
                11 -> {
                    val reg = "(^\\d{1,2}.)".toRegex().find(string.substring(startIndex, endIndex))
                    val order = reg!!.value
                    text =
                        string.subSequence(startIndex.plus(order.length.inc()), endIndex).toString()

                    val subs =
                        findElements(
                            text
                        )
                    val element =
                        Element.OrderedListItem(
                            order,
                            text.toString(),
                            subs
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //12 -> NUMERIC LIST
                12 -> {
                    text = string.subSequence(startIndex, endIndex)

                    val (alt, url, title) = "^!\\[([^\\[\\]]*?)?]\\((.*?) \"(.*?)\"\\)$".toRegex()
                        .find(text)!!.destructured

                    val element = Element.Image(url, if (alt.isBlank()) null else alt, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
            }

        }

        //all other is Text
        if (lastStartIndex < string.length){
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(
                Element.Text(
                    text
                )
            )
        }
        return parents
    }
}

sealed class MarkdownElement(){
    abstract val offset: Int
    val bounds: Pair<Int, Int> by lazy {
        when (this){
            is Text -> {
                val end = elements.fold(offset){acc, el ->
                    acc + el.spread().map { it.text.length}.sum()
                }
                offset to end
            }

            is Image -> offset to image.text.length + offset

            is Scroll -> offset to blockCode.text.length + offset
        }
    }

    data class Text(
        val elements: MutableList<Element>,
        override val offset: Int = 0
    ) : MarkdownElement()

    data class Image(
        val image: Element.Image,
        override val offset: Int = 0
    ) : MarkdownElement()

    data class Scroll(
        val blockCode: Element.BlockCode,
        override val offset: Int = 0
    ) : MarkdownElement()
}
//data class MarkdownText(val elements: List<Element>)

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
/*
    data class BlockCode(
        val type: Type = Type.MIDDLE,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element() {
        enum class Type { START, END, MIDDLE, SINGLE }
    }
    */
    data class BlockCode(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Image(
        val url: String,
        val alt: String?,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()
}

private fun Element.spread(): List<Element>{
    val elements = mutableListOf<Element>()
    if (this.elements.isNotEmpty()) elements.addAll(this.elements.spread())
    else elements.add(this)
    return elements
}

private fun List<Element>.spread(): List<Element>{
    val elements = mutableListOf<Element>()
    forEach{elements.addAll(it.spread())}
    return elements
}

private fun Element.clearContent() : String {
    return StringBuilder().apply{
        val element = this@clearContent
        if (element.elements.isEmpty()) append(element.text)
        else element.elements.forEach{ append(it.clearContent())}
    }.toString()
}

fun List<MarkdownElement>.clearContent() : String{
    return StringBuilder().apply{
        this@clearContent.forEach{
            when (it){
                is MarkdownElement.Text -> it.elements.forEach{el -> append(el.clearContent())}
                is MarkdownElement.Image -> append(it.image.clearContent())
                is MarkdownElement.Scroll -> append(it.blockCode.clearContent())
            }
        }
    }.toString()
}