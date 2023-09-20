package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache

class MathParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()
        var iterator: TokensCache.Iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.DOLLAR || iterator.type == MarkdownTokenTypes.ESCAPED_DOLLAR) {

                val endIterator = findOfSize(iterator.advance(), getLength(iterator, true))

                if (endIterator != null) {
                    if (iterator.length == 1) {
                        result.withNode(SequentialParser.Node(iterator.index..endIterator.index + 1, MarkdownElementTypes.INLINE_MATH))
                    } else {
                        result.withNode(SequentialParser.Node(iterator.index..endIterator.index + 1, MarkdownElementTypes.BLOCK_MATH))
                    }
                    iterator = endIterator.advance()
                    continue
                }
            }
            delegateIndices.put(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(delegateIndices.get())
    }

    private fun findOfSize(it: TokensCache.Iterator, length: Int): TokensCache.Iterator? {
        var iterator = it
        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.DOLLAR) {
                if (getLength(iterator, true) == length) {
                    return iterator
                }
            }

            iterator = iterator.advance()
        }
        return null
    }


    private fun getLength(info: TokensCache.Iterator, canEscape: Boolean): Int {
        var toSubtract = 0
        if (info.type == MarkdownTokenTypes.ESCAPED_DOLLAR) {
            toSubtract = if (canEscape) {
                2
            } else {
                1
            }
        }

        return info.length - toSubtract
    }
}
