package cn.revaria.chatplus.mixin.compat;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cn.revaria.chatplus.util.TextStyleFormatter.applyStyle;

@Pseudo
@Mixin(targets =
	#if MC_VER <= MC_1_19
		"org.dynmap.fabric_1_19.DynmapPlugin$ChatHandler"
	#elif MC_VER <= MC_1_19_1
		"org.dynmap.fabric_1_19_1.DynmapPlugin$ChatHandler"
	#elif MC_VER <= MC_1_19_3
		{"org.dynmap.fabric_1_19_3.DynmapPlugin$ChatHandler", "org.dynmap.fabric_1_19_4.DynmapPlugin$ChatHandler"}
	#elif MC_VER <= MC_1_20
		{"org.dynmap.fabric_1_20.DynmapPlugin$ChatHandler", "org.dynmap.fabric_1_20_2.DynmapPlugin$ChatHandler"}
	#elif MC_VER <= MC_1_20_3
		{"org.dynmap.fabric_1_20_4.DynmapPlugin$ChatHandler", "org.dynmap.fabric_1_20_6.DynmapPlugin$ChatHandler"}
	#elif MC_VER <= MC_1_21
		{
			"org.dynmap.fabric_1_21.DynmapPlugin$ChatHandler",
			"org.dynmap.fabric_1_21_1.DynmapPlugin$ChatHandler",
			"org.dynmap.fabric_1_21_3.DynmapPlugin$ChatHandler",
			"org.dynmap.fabric_1_21_5.DynmapPlugin$ChatHandler"
		}
	#endif
)
public abstract class DynmapMixin {
	@Inject(method = "handleChat", at = @At("HEAD"), remap = false)
	private void modifyMessage(ServerPlayerEntity player, String message, CallbackInfo ci, @Local(argsOnly = true) LocalRef<String> messageRef) {
		messageRef.set(applyStyle(Text.of(message), player).getString());
	}
}
