package cn.revaria.chatplus.mixin;

#if MC_VER <= MC_1_19
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.util.registry.RegistryKey;
#endif

import cn.revaria.chatplus.plugin.annotation.DisableIfModsLoaded;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static cn.revaria.chatplus.util.TextStyleFormatter.applyStyle;

@DisableIfModsLoaded("styledchat")
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ChatMixin {
	@Redirect(method = "handleDecoratedMessage", at = @At(value = "INVOKE", target =
		#if MC_VER <= MC_1_19
			"Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/server/filter/FilteredMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/registry/RegistryKey;)V"
		#else
			"Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V"
		#endif
	))
	#if MC_VER <= MC_1_19
	private void replaceText(PlayerManager instance, FilteredMessage<SignedMessage> message, ServerPlayerEntity sender, RegistryKey<MessageType> typeKey) {
		var newMessage = new FilteredMessage<>(
			message.raw().withUnsigned(applyStyle(message.raw().getContent(), sender)),
			message.filtered() != null ? message.filtered().withUnsigned(applyStyle(message.filtered().getContent(), sender)) : null
		);
		instance.broadcast(newMessage, sender, typeKey);
	}
	#else
	private void replaceText(PlayerManager instance, SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) {
		instance.broadcast(message.withUnsignedContent(applyStyle(message.getContent(), sender)), sender, params);
	}
	#endif
}
// 拦截玩家发送的聊天数据包，在消息发送前将其中的 & 颜色代码和 [item] 占位符替换为 Minecraft 内部的文本样式组件。