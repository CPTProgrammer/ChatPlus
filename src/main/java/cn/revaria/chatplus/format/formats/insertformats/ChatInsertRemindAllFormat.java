package cn.revaria.chatplus.format.formats.insertformats;

import cn.revaria.chatplus.ChatPlus;
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

public class ChatInsertRemindAllFormat implements ChatInsertFormat {

	private final String reminderText;
	private boolean soundPlayed = false;

	private final int startingIndex;
	private final ChatFormatType formatType = ChatFormatType.INSERT;

	public ChatInsertRemindAllFormat(int startingIndex, String reminderText) {
		this.startingIndex = startingIndex;

		this.reminderText = reminderText;
	}

	@Override
	public MutableComponent getInsertComponent() {
		if (!soundPlayed) {
			soundPlayed = true;

			for (ServerPlayer player : ChatPlus.getServer().getPlayerList().getPlayers()) {
				player.connection.send(new ClientboundSoundPacket(
					Holder.direct(SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("minecraft", "entity.experience_orb.pickup"))),
					SoundSource.MASTER,
					player.position().x,
					player.position().y,
					player.position().z,
					3,
					1,
					player.level().getRandom().nextLong()
				));
			}
		}

		return Component.literal("@").append(reminderText)
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
		return new ChatInsertRemindAllFormat(startingIndex, reminderText);
	}
}
