package cn.revaria.chatplus.mixin.compat;

#if MC_VER <= MC_1_21
import eu.pb4.placeholders.api.PlaceholderContext;
#else
import eu.pb4.placeholders.api.ServerPlaceholderContext;
#endif

import eu.pb4.styledchat.StyledChatUtils;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static cn.revaria.chatplus.util.TextStyleFormatter.applyStyle;

@Pseudo
@Mixin(StyledChatUtils.class)
public abstract class StyledChatMixin {
	@Inject(method =
		#if MC_VER <= MC_1_21
			"formatFor(Leu/pb4/placeholders/api/PlaceholderContext;Ljava/lang/String;)Lnet/minecraft/network/chat/Component;"
		#else
			"formatFor(Leu/pb4/placeholders/api/ServerPlaceholderContext;Ljava/lang/String;)Lnet/minecraft/network/chat/Component;"
		#endif,
		at = @At("RETURN"),
		cancellable = true
	)
	private static void modifyText(
		#if MC_VER <= MC_1_21 PlaceholderContext #else ServerPlaceholderContext #endif context,
		String input, CallbackInfoReturnable<Component> cir
	) {
		cir.setReturnValue(applyStyle(cir.getReturnValue(), context.#if MC_VER <= MC_1_21 player() #else serverPlayer() #endif));
	}
}
