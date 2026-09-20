package cn.revaria.chatplus.mixin;

#if MC_VER <= MC_1_19
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.FilteredText;
#endif

import cn.revaria.chatplus.plugin.annotation.DisableIfModsLoaded;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static cn.revaria.chatplus.util.TextStyleFormatter.applyStyle;

@DisableIfModsLoaded("styledchat")
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ChatMixin {
	@Redirect(method = "broadcastChatMessage", at = @At(value = "INVOKE", target =
		#if MC_VER <= MC_1_19
			"Lnet/minecraft/server/players/PlayerList;broadcastChatMessage(Lnet/minecraft/server/network/FilteredText;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/resources/ResourceKey;)V"
		#else
			"Lnet/minecraft/server/players/PlayerList;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V"
		#endif
	))
	#if MC_VER <= MC_1_19
	private void replaceText(PlayerList instance, FilteredText<PlayerChatMessage> message, ServerPlayer sender, ResourceKey<ChatType> typeKey) {
		var newMessage = new FilteredText<>(
			message.raw().withUnsignedContent(applyStyle(message.raw().serverContent(), sender)),
			message.filtered() != null ? message.filtered().withUnsignedContent(applyStyle(message.filtered().serverContent(), sender)) : null
		);
		instance.broadcastChatMessage(newMessage, sender, typeKey);
	}
	#else
	private void replaceText(PlayerList instance, PlayerChatMessage message, ServerPlayer sender, ChatType.Bound params) {
		instance.broadcastChatMessage(
			message.withUnsignedContent(
				applyStyle(message.#if MC_VER <= MC_1_19_1 serverContent() #else decoratedContent() #endif, sender)
			),
			sender, params
		);
	}
	#endif
}
// 拦截玩家发送的聊天数据包，在消息发送前将其中的 & 颜色代码和 [item] 占位符替换为 Minecraft 内部的文本样式组件。