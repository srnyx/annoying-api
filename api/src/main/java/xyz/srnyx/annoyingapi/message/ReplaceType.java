package xyz.srnyx.annoyingapi.message;

import org.jetbrains.annotations.NotNull;

import java.util.function.BinaryOperator;


/**
 * Used in {@link AnnoyingMessage#replace(String, Object, ReplaceType)}
 * <p>Implement this into your own enum to create your own {@link ReplaceType}s for {@link AnnoyingMessage#replace(String, Object, ReplaceType)}
 *
 * @see DefaultReplaceType
 * @see AnnoyingMessage#replace(String, Object, ReplaceType)
 */
public interface ReplaceType {
    /**
     * If no input is provided, this will be used
     *
     * @return the default input
     */
    @NotNull
    String getDefaultInput();

    /**
     * The action done with the input and value
     * <p>The input comes first, then the value (for the operands)
     *
     * @return the {@link BinaryOperator} to use on the input and value
     */
    @NotNull
    BinaryOperator<String> getOutputOperator();
}
