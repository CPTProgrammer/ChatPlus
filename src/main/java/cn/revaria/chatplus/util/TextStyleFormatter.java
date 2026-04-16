package cn.revaria.chatplus.util;

#if MC_VER <= MC_1_20
import net.minecraft.network.chat.contents.LiteralContents;
#else
import net.minecraft.network.chat.contents.PlainTextContents;
#endif

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextStyleFormatter {
	public static int MAIN_HAND = -1; // Must be smaller than or equal to 0

	/**
	 * Processes text styling with two key functions:
	 * <p>
	 *     1. Replaces {@code &} with {@code §}, using {@code &&} to escape literal {@code &}<br>
	 *     2. Substitutes {@code [item]} with main-hand item hover text and {@code [item=N]} with Nth slot's hover text
	 * </p>
	 * Recursively processes nested text components.
	 *
	 * @param sourceText Original text to process (supports nested text components)
	 * @param sourcePlayer Player used for item stack references
	 * @return Processed text with styling and item hover elements
	 */
	public static MutableComponent applyStyle(Component sourceText, ServerPlayer sourcePlayer) {
		MutableComponent sourceMutableText = sourceText.copy();

		MutableComponent finalText = Component.empty().setStyle(sourceMutableText.getStyle());

		if (sourceText.getContents() instanceof #if MC_VER <= MC_1_20 LiteralContents #else PlainTextContents #endif plainTextContent) {
			String changedMessage = plainTextContent.text()
				.replace('&', '§')
				.replace("§§", "&");
			String regex = "\\[item(?:=([1-9]))?\\]";
			String[] messages = changedMessage.split(regex, -1);
			Deque<Integer> itemDeque = new ArrayDeque<>();

			Matcher matcher = Pattern.compile(regex).matcher(changedMessage);
			while (matcher.find()) {
				String digit = matcher.group(1);
				if (digit == null) {
					itemDeque.addLast(MAIN_HAND);
				} else {
					itemDeque.addLast(Integer.parseInt(digit));
				}
			}

			for (String message : messages) {
				finalText.append(Component.literal(message));
				if (!itemDeque.isEmpty()) {
					ItemStack itemStack;
					if (itemDeque.getFirst() == MAIN_HAND) {
						itemStack = sourcePlayer.getMainHandItem();
					} else {
						itemStack = sourcePlayer.getInventory().getItem(itemDeque.getFirst() - 1);
					}
					finalText.append(itemStack.getDisplayName());
					itemDeque.removeFirst();
				}
			}
		}

		List<Component> sourceTexts = sourceMutableText.getSiblings();
		for (Component text : sourceTexts) {
			finalText.append(applyStyle(text, sourcePlayer));
		}

		return finalText;
	}
}
