package xyz.srnyx.annoyingapi.file.okaeri.serdes.itemstack.spec;

import xyz.srnyx.annoyingapi.file.okaeri.serdes.itemstack.transformer.ItemStackTransformer;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.itemstack.transformer.NoopItemStackTransformer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ItemStackSpec {
    Class<? extends ItemStackTransformer> transformer() default NoopItemStackTransformer.class;
}
