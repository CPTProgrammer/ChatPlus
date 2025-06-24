package cn.revaria.chatplus.plugin;

import cn.revaria.chatplus.plugin.annotation.DisableIfModsLoaded;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MixinConfigPlugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		try {
			ClassNode classNode = MixinService.getService().getBytecodeProvider().getClassNode(mixinClassName);
			AnnotationNode annotationNode = Annotations.getVisible(classNode, DisableIfModsLoaded.class);

			if (annotationNode == null) {
				return true;
			}

			List<String> modIds = Annotations.getValue(annotationNode);
			return modIds.stream().noneMatch(modId -> FabricLoader.getInstance().isModLoaded(modId));
		} catch (IOException | ClassNotFoundException e) {
			return false;
		}
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}
