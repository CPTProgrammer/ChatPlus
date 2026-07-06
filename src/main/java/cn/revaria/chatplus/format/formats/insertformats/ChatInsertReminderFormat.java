package cn.revaria.chatplus.format.formats.insertformats;

import cn.revaria.chatplus.format.ChatFormatType;
import cn.revaria.chatplus.format.formats.ChatInsertFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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

			aimPlayer.connection.send(new ClientboundSoundPacket(
				Holder.direct(SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("minecraft", "entity.experience_orb.pickup"))),
				SoundSource.MASTER,
				aimPlayer.position().x,
				aimPlayer.position().y,
				aimPlayer.position().z,
				3,
				1,
				aimPlayer.level().getRandom().nextLong()
			));
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
