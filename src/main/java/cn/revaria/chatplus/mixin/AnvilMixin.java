package cn.revaria.chatplus.mixin;

import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cn.revaria.chatplus.util.TextStyleFormatter.applySimpleStyle;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilMixin extends ScreenHandler {
	@Shadow
	@Nullable
	private String newItemName;

	@Shadow
	@Final
	private Property levelCost;

	protected AnvilMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
		super(type, syncId);
	}

	@Inject(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0))
	private void applyColorToName(CallbackInfo ci) {
		if (this.newItemName != null && !this.newItemName.isEmpty()) {
			String formattedName = applySimpleStyle(this.newItemName);
			if (!formattedName.equals(this.newItemName)) {
				this.newItemName = formattedName;
				// In some versions, we might need to ensure the level cost is updated or the result is recalculated.
				// But usually, modifying newItemName before the result item is constructed is enough.
			}
		}
	}
}
