/*
 *     Kraft: Lightweight Minecraft client for Android featuring modules support and other task automation
 *     Copyright (C) 2020  Cubxity
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.cubxity.kraft.utils

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.core.text.buildSpannedString
import com.github.steveice10.mc.protocol.data.message.*
import java.util.*

object ChatUtils {
    val translations = mapOf(
        "chat.stream.emote" to "(%s) * %s %s",
        "chat.stream.text" to "(%s) <%s> %s",
        "chat.type.achievement" to "%s has just earned the achievement %s",
        "chat.type.admin" to "[%s: %s]",
        "chat.type.announcement" to "[%s] %s",
        "chat.type.emote" to "* %s %s",
        "chat.type.text" to "<%s> %s",
        "multiplayer.player.joined" to "%s joined the game.",
        "multiplayer.player.left" to "%s left the game."
    )

    fun buildSpan(message: Message): SpannedString = buildSpannedString {
        val queue = LinkedList<Message>()
        queue += message

        while (queue.isNotEmpty()) {
            val msg = queue.remove()

            val style = msg.style

            val startIndex = length
            if (msg is TranslationMessage) {
                val translation = translations[msg.translationKey]
                if (translation != null) {
                    append(translation.format(*msg.translationParams.map { buildSpan(it) }
                        .toTypedArray()))
                } else {
                    append(msg.text)
                }
            } else {
                append(msg.text)
            }

            applySpan(style, this, startIndex, length)

            queue += msg.extra
        }
    }

    fun applySpan(
        style: MessageStyle,
        builder: SpannableStringBuilder,
        startIndex: Int,
        endIndex: Int
    ) {
        val color = convertColor(style.color)
        builder.setSpan(ForegroundColorSpan(color), startIndex, endIndex, 0)

        var flags = 0
        style.formats.forEach {
            when (it) {
                ChatFormat.BOLD -> flags = flags or Typeface.BOLD
                ChatFormat.UNDERLINED -> {
                    builder.setSpan(UnderlineSpan(), startIndex, endIndex, 0)
                }
                ChatFormat.STRIKETHROUGH -> {
                    builder.setSpan(StrikethroughSpan(), startIndex, endIndex, 0)
                }
                ChatFormat.ITALIC -> flags = flags or Typeface.ITALIC
            }
        }

        builder.setSpan(StyleSpan(color), startIndex, endIndex, 0)
    }

    fun convertColor(color: ChatColor) = when (color) {
        ChatColor.BLACK -> 0
        ChatColor.DARK_BLUE -> 0xFF536DFE
        ChatColor.DARK_GREEN -> 0xFF69F0AE
        ChatColor.DARK_AQUA -> 0xFF18FFFF
        ChatColor.DARK_RED -> 0xFFFF5252
        ChatColor.DARK_PURPLE -> 0xFF7C4DFF
        ChatColor.GOLD -> 0xFFFFD740
        ChatColor.GRAY -> 0xFFEEEEEE
        ChatColor.DARK_GRAY -> 0xFF333333
        ChatColor.BLUE -> 0xFF80D8FF
        ChatColor.GREEN -> 0xFFCCFF90
        ChatColor.AQUA -> 0xFF84FFFF
        ChatColor.RED -> 0xFFFF8A80
        ChatColor.LIGHT_PURPLE -> 0xFFB388FF
        ChatColor.YELLOW -> 0xFFF4FF81
        ChatColor.WHITE -> 0xFFFFFFFF
        else -> 0xFFFFFFFF
    }.toInt()
}