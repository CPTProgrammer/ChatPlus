package cn.revaria.chatplus.format.formats;

import cn.revaria.chatplus.format.ChatFormat;
import cn.revaria.chatplus.format.ChatFormatType;
import net.minecraft.ChatFormatting;

import java.util.Map;

public class ChatFontFormat implements ChatFormat {

	private final ChatFormatting fontFormat;

	private final int startingIndex;
	private final ChatFormatType formatType = ChatFormatType.FONT;

	public ChatFontFormat(int startingIndex, ChatFormatting fontFormat) {
		this.startingIndex = startingIndex;

		this.fontFormat = fontFormat;
	}
	public ChatFontFormat(int startingIndex, char fontCode) {
		this.startingIndex = startingIndex;

		this.fontFormat = VANILLA_FONT_FORMATS.getOrDefault(fontCode, ChatFormatting.ITALIC);
	}

	public ChatFormatting getFont() {
		return fontFormat;
	}

	@Override
	public int startingIndex() {
		return startingIndex;
	}
	@Override
	public ChatFormatType formatType() {
		return formatType;
	}
	@Override
	public ChatFontFormat copy() {
		return new ChatFontFormat(this.startingIndex, this.fontFormat);
	}

	public static final Map<Character, ChatFormatting> VANILLA_FONT_FORMATS = Map.ofEntries(
		Map.entry('k', ChatFormatting.OBFUSCATED),
		Map.entry('l', ChatFormatting.BOLD),
		Map.entry('m', ChatFormatting.STRIKETHROUGH),
		Map.entry('n', ChatFormatting.UNDERLINE),
		Map.entry('o', ChatFormatting.ITALIC)
	);
}
