package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.spec;

import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.transformer.result.NoopResultTransformer;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.transformer.result.ResultTransformer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RecipeSpec {
    /**
     * The name/key of the recipe
     */
    String name();

    /**
     * If you don't want the result saved in the recipe, you can add {@link RecipeFeature#RESULT} to {@link #disabledFeatures()}
     * <br><i>If you do, the result transformer MUST be non-null!</i>
     */
    Class<? extends ResultTransformer> resultTransformer() default NoopResultTransformer.class;

    /**
     * If {@link RecipeFeature#RESULT} is disabled, there MUST be a {@link ResultTransformer} specified that is non-null (recipes must have a result when constructed)
     */
    RecipeFeature[] disabledFeatures() default {};
}
