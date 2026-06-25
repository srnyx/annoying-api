package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe;

import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.transformer.result.NoopResultTransformer;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.transformer.result.ResultTransformer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RecipeSpec {
    String name();

    Class<? extends ResultTransformer> resultTransformer() default NoopResultTransformer.class;
}
