package cn.revaria.chatplus.util;

#if MC_VER <= MC_1_20
import net.minecraft.network.chat.contents.LiteralContents;
#else
import cn.revaria.chatplus.format.ChatFormat;
import cn.revaria.chatplus.format.ChatFormatType;
import cn.revaria.chatplus.format.formats.ChatColorFormat;
import cn.revaria.chatplus.format.formats.ChatFontFormat;
import cn.revaria.chatplus.format.formats.ChatInsertItemFormat;
import cn.revaria.chatplus.format.formats.ChatResetFormat;
#endif

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextStyleFormatter {

	/**
	 *
	 *
	 * @param sourceText Original text to process (supports nested text components)
	 * @param sourcePlayer Player used for item stack references
	 * @return Processed text with styling and item hover elements
	 */
	public static MutableComponent applyStyle(Component sourceText, ServerPlayer sourcePlayer) {
		MutableComponent sourceMutableText = sourceText.copy();

		String sourceRawText = sourceMutableText.toString().substring(8, sourceMutableText.toString().length() - 1);

		// These will be motified in translateInput()
		TreeMap<Integer, ArrayList<ChatFormat>> formatsTable = new TreeMap<>();

		String sourceLiteralText = translateInput(sourceRawText, sourcePlayer, formatsTable);

		/* Debug
		sourcePlayer.sendSystemMessage(Component.literal(sourceLiteralText));
		for (int i : formatsTable.keySet()) {
			ArrayList<ChatFormat> formatsList = formatsTable.get(i);
			sourcePlayer.sendSystemMessage(Component.literal(i + "."));
			for (ChatFormat format : formatsList) {
				if (format instanceof ChatInsertItemFormat) {
					sourcePlayer.sendSystemMessage(Component.literal("  物品插入数据"));
				}
				if (format instanceof ChatResetFormat) {
					sourcePlayer.sendSystemMessage(Component.literal("  清除"));
				}
				if (format instanceof ChatColorFormat colorFormat) {
					sourcePlayer.sendSystemMessage(Component.literal("  颜色:" + colorFormat.getR() + " " + colorFormat.getG() + " " + colorFormat.getB()));
				}
				if (format instanceof ChatFontFormat fontFormat) {
					sourcePlayer.sendSystemMessage(Component.literal("  字体").withStyle(fontFormat.getFont()));
				}
			}
		}*/

		MutableComponent finalText = Component.empty().setStyle(sourceMutableText.getStyle());
		ChatColorFormat currentColor = new ChatColorFormat(0, 'f');
		HashSet<ChatFontFormat> currentFonts = new HashSet<>();
		int startIndex = 0;

		for (int i : formatsTable.keySet()) {

			ArrayList<ChatFormat> formatsList = formatsTable.get(i);

			if (formatsList.getFirst() instanceof ChatInsertItemFormat insertItem) {

				if (i > 0) {
					finalText.append(composeChatComponent(
						sourceLiteralText.substring(startIndex, i),
						currentColor,
						currentFonts
						));
				}

				finalText.append(insertItem.getItem().getDisplayName());

				startIndex = i;
				continue;
			}
			if (formatsList.getFirst() instanceof ChatResetFormat) {

				if (i > 0) {
					finalText.append(composeChatComponent(
						sourceLiteralText.substring(startIndex, i),
						currentColor,
						currentFonts
					));
				}

				currentColor = new ChatColorFormat(0, 'f');
				currentFonts.clear();

				startIndex = i;
				continue;
			}

			if (i > 0) {
				finalText.append(composeChatComponent(
					sourceLiteralText.substring(startIndex, i),
					currentColor,
					currentFonts
				));
			}

			for (ChatFormat format : formatsList) {

				if (format instanceof ChatColorFormat colorFormat) {
					currentColor = colorFormat;
				}
				else if (format instanceof ChatFontFormat fontFormat) {
					currentFonts.add(fontFormat);
				}
			}

			startIndex = i;
		}
		if (startIndex < sourceLiteralText.length() - 1) {
			finalText.append(composeChatComponent(
				sourceLiteralText.substring(startIndex, sourceLiteralText.length()),
				currentColor,
				currentFonts
			));
		}

		return finalText;
	}

	private static MutableComponent composeChatComponent(String chatText, ChatColorFormat currentColor, HashSet<ChatFontFormat> currentFonts) {

		if (currentColor == null) {
			currentColor = new ChatColorFormat(0, 'f');
		}

		MutableComponent finalChatComponent = Component.literal(chatText);

		finalChatComponent.withColor(currentColor.getR() << 16 | currentColor.getG() << 8 | currentColor.getB());
		for (ChatFontFormat fontFormat : currentFonts) {
			finalChatComponent.withStyle(fontFormat.getFont());
		}

		return finalChatComponent;
	}

	private static String translateInput(String sourceRawText, ServerPlayer sourcePlayer, TreeMap<Integer, ArrayList<ChatFormat>> formatsTableOutput) {

		StringBuilder textBuilder = new StringBuilder();

		String itemRegex = "\\[item(?:=([1-9]))?\\]";
		Pattern itemPattern = Pattern.compile(itemRegex);
		Matcher matcher = itemPattern.matcher(sourceRawText);

		for (int i = 0; i < sourceRawText.length(); ++i) {

			char character = sourceRawText.charAt(i);
			int currentIndex = textBuilder.length();

			if (character == '&' && i < sourceRawText.length() - 1) {

				char colorCode = sourceRawText.charAt(i + 1);
				if (ChatColorFormat.VANILLA_COLOR_FORMATS.containsKey(colorCode)) {

					if (!formatsTableOutput.containsKey(currentIndex)) {
						formatsTableOutput.put(currentIndex, new ArrayList<>());
					}
					formatsTableOutput.get(currentIndex).add(new ChatColorFormat(currentIndex, colorCode));

					++i;
					continue;
				}
			}

			if (character == '#' && i < sourceRawText.length() - 6) {

				boolean legalColorCode = true;
				for (int j = i + 1; j <= i + 6 && j < sourceRawText.length(); ++j) {
					char c = sourceRawText.charAt(j);

					if (!((c >= '0' && c <= '9') ||
						(c >= 'a' && c <= 'f') ||
						(c >= 'A' && c <= 'F'))) {
						legalColorCode = false;
						break;
					}
				}

				if (legalColorCode) {

					if (!formatsTableOutput.containsKey(currentIndex)) {
						formatsTableOutput.put(currentIndex, new ArrayList<>());
					}

					int color = Integer.parseInt(sourceRawText.substring(i + 1, i + 7), 16);
					formatsTableOutput.get(currentIndex).add(new ChatColorFormat(
						currentIndex,
						(color >> 16) & 0xFF,
						(color >> 8) & 0xFF,
						color & 0xFF)
					);

					i += 6;
					continue;
				}
			}

			if (character == '&' && i < sourceRawText.length() - 1) {

				char fontCode = sourceRawText.charAt(i + 1);
				if (ChatFontFormat.VANILLA_FONT_FORMATS.containsKey(fontCode)) {

					if (!formatsTableOutput.containsKey(currentIndex)) {
						formatsTableOutput.put(currentIndex, new ArrayList<>());
					}
					formatsTableOutput.get(currentIndex).add(new ChatFontFormat(currentIndex, fontCode));

					++i;
					continue;
				}
			}

			if (character == '[' && i < sourceRawText.length() - 5) {

				matcher.region(i, Math.min(i + 8, sourceRawText.length()));
				if (matcher.lookingAt()) {

					if (!formatsTableOutput.containsKey(currentIndex)) {
						formatsTableOutput.put(currentIndex, new ArrayList<>());
					}

					String digit = matcher.group(1);
					if (digit == null) {
						formatsTableOutput.get(currentIndex).add(new ChatInsertItemFormat(
							currentIndex,
							sourcePlayer.getMainHandItem()
						));
					}
					else {
						formatsTableOutput.get(currentIndex).add(new ChatInsertItemFormat(
							currentIndex,
							sourcePlayer.getInventory().getItem(Integer.parseInt(digit) - 1)
						));
					}

					while (sourceRawText.charAt(i) != ']' && i < sourceRawText.length()) {
						++i;
					}
					continue;
				}
			}

			if (character == '&' && i < sourceRawText.length() - 1) {

				if (sourceRawText.charAt(i + 1) == 'r') {

					if (!formatsTableOutput.containsKey(currentIndex)) {
						formatsTableOutput.put(currentIndex, new ArrayList<>());
					}
					formatsTableOutput.get(currentIndex).add(new ChatResetFormat(currentIndex));

					++i;
					continue;
				}
			}

			textBuilder.append(character);
		}

		formatsTableOutput.forEach((i, formatsList) -> {

			boolean containsResetFormat = false;
			ChatInsertItemFormat containedInsertItemFormat = null;
			for (ChatFormat format : formatsList) {

				if (format.formatType() == ChatFormatType.RESET) {
					containsResetFormat = true;
					break;
				}
				if (format instanceof ChatInsertItemFormat insertItemFormat && format.formatType() == ChatFormatType.INSERT) {
					containedInsertItemFormat = insertItemFormat.copy();
					break;
				}
			}

			if (containsResetFormat) {
				formatsList.clear();
				formatsList.add(new ChatResetFormat(i));
			}
			if (containedInsertItemFormat != null) {
				formatsList.clear();
				formatsList.add(containedInsertItemFormat);
			}
		});

		return textBuilder.toString();
	}
}
