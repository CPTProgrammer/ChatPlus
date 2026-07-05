package cn.revaria.chatplus.format.formats.insertformats;

import cn.revaria.chatplus.format.ChatFormatType;
import cn.revaria.chatplus.format.formats.ChatInsertFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class ChatInsertReminderFormat implements ChatInsertFormat {

	private final ServerPlayer aimPlayer;
	private boolean soundPlayed = false;

	private final int startingIndex;
	private final ChatFormatType formatType = ChatFormatType.INSERT;

	public ChatInsertReminderFormat(int startingIndex, ServerPlayer aim) {
		this.startingIndex = startingIndex;

		this.aimPlayer = aim;
	}

	public ServerPlayer getAimPlayer() {
		return aimPlayer;
	}

	@Override
	public MutableComponent getInsertComponent() {
		if (!soundPlayed) {
			soundPlayed = true;
			aimPlayer.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 3, 1);
		}
		return Component.literal("@").append(aimPlayer.getDisplayName())
			.withColor(TextColor.GREEN)
			.withStyle(ChatFormatting.BOLD);
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
	public ChatInsertFormat copy() {
		return new ChatInsertReminderFormat(startingIndex, aimPlayer);
	}
}
