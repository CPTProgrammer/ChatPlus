package cn.revaria.chatplus.mixin.compat;

import eu.pb4.placeholders.api.PlaceholderContext;
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
	@Inject(method = "formatFor(Leu/pb4/placeholders/api/PlaceholderContext;Ljava/lang/String;)Lnet/minecraft/network/chat/Component;", at = @At("RETURN"), cancellable = true)
	private static void modifyText(PlaceholderContext context, String input, CallbackInfoReturnable<Component> cir) {
		cir.setReturnValue(applyStyle(cir.getReturnValue(), context.player()));
	}
}
