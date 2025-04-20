package cn.revaria.chatplus.plugin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Conditionally disables the annotated mixin when specified mods are loaded.
 *
 * <p>When applied, the mixin config plugin should disable the annotated class
 * if any mod ID in the value list is present in the runtime environment.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DisableIfModsLoaded {
	String[] value();
}
