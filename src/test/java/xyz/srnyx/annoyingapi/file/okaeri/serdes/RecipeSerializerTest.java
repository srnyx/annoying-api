package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigBuilder;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigTestSupport;
import xyz.srnyx.annoyingapi.file.okaeri.MockBukkitTestSupport;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.RecipeSerializer;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.RecipeSpec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * Tests for {@link RecipeSerializer}.
 *
 * <p>Requires MockBukkit for Bukkit object construction and Mockito for {@link AnnoyingPlugin}.
 * The serializer is injected via the {@code configure()} callback since ConfigBuilder only
 * registers it when {@code plugin != null}.
 */
public class RecipeSerializerTest extends MockBukkitTestSupport {
    @TempDir Path tempDir;

    // ------------------------------------------------------------------ Test config classes

    public static class ShapelessConfig extends OkaeriConfig {
        @RecipeSpec(name = "test_shapeless")
        public Recipe recipe = null;
    }

    public static class ShapedConfig extends OkaeriConfig {
        @RecipeSpec(name = "test_shaped")
        public Recipe recipe = null;
    }

    public static class FurnaceConfig extends OkaeriConfig {
        @RecipeSpec(name = "test_furnace")
        public FurnaceRecipe recipe = null;
    }

    // ------------------------------------------------------------------ Load helper

    private <C extends OkaeriConfig> C load(String yaml, Class<C> cls) throws IOException {
        final AnnoyingPlugin mockPlugin = mock(AnnoyingPlugin.class);
        when(mockPlugin.getName()).thenReturn("test");
        final Path path = ConfigTestSupport.writeYaml(tempDir, cls.getSimpleName() + ".yml", yaml);
        final RecipeSerializer serializer = new RecipeSerializer(mockPlugin);
        return new ConfigBuilder(new File(path.toString()))
                .config(cls)
                .configure(opt -> opt.serdes(serializer))
                .build();
    }

    private ShapelessConfig loadShapeless(String yaml) throws IOException {
        return load(yaml, ShapelessConfig.class);
    }

    private ShapedConfig loadShaped(String yaml) throws IOException {
        return load(yaml, ShapedConfig.class);
    }

    // ------------------------------------------------------------------ ShapelessRecipe

    @Test
    void shapelessRecipe_1Ingredient() throws IOException {
        final ShapelessConfig config = loadShapeless("recipe:\n  shapeless: true\n  shape:\n    - \"S\"\n  ingredients:\n    S: STONE\n  result:\n    material: DIAMOND\n    amount: 1");

        assertNotNull(config.recipe);
        assertInstanceOf(ShapelessRecipe.class, config.recipe);
        final ShapelessRecipe sr = (ShapelessRecipe) config.recipe;
        assertEquals(1, sr.getIngredientList().size());
        assertEquals(Material.STONE, sr.getIngredientList().get(0).getType());
    }

    @Test
    void shapelessRecipe_result_isDiamond() throws IOException {
        final ShapelessConfig config = loadShapeless("recipe:\n  shapeless: true\n  shape:\n    - \"S\"\n  ingredients:\n    S: STONE\n  result:\n    material: DIAMOND\n    amount: 1");

        assertEquals(Material.DIAMOND, config.recipe.getResult().getType());
    }

    @Test
    void shapelessRecipe_2SameMaterialIngredients() throws IOException {
        // shape "SS" → 2 occurrences of S
        final ShapelessConfig config = loadShapeless("recipe:\n  shapeless: true\n  shape:\n    - \"SS\"\n  ingredients:\n    S: STONE\n  result:\n    material: DIAMOND\n    amount: 1");
        final ShapelessRecipe sr = (ShapelessRecipe) config.recipe;

        assertEquals(2, sr.getIngredientList().size());
        sr.getIngredientList().forEach(i -> assertEquals(Material.STONE, i.getType()));
    }

    @Test
    void shapelessRecipe_3DifferentIngredients() throws IOException {
        final ShapelessConfig config = loadShapeless("recipe:\n  shapeless: true\n  shape:\n    - \"ABC\"\n  ingredients:\n    A: STONE\n    B: OAK_LOG\n    C: GOLD_INGOT\n  result:\n    material: DIAMOND\n    amount: 1");
        final ShapelessRecipe sr = (ShapelessRecipe) config.recipe;

        assertEquals(3, sr.getIngredientList().size());
        final List<Material> types = sr.getIngredientList().stream()
                .map(i -> i.getType())
                .toList();
        assertTrue(types.contains(Material.STONE));
        assertTrue(types.contains(Material.OAK_LOG));
        assertTrue(types.contains(Material.GOLD_INGOT));
    }

    @Test
    void shapelessRecipe_result_respectsAmount() throws IOException {
        final ShapelessConfig config = loadShapeless("recipe:\n  shapeless: true\n  shape:\n    - \"S\"\n  ingredients:\n    S: STONE\n  result:\n    material: DIAMOND\n    amount: 4");

        assertEquals(4, config.recipe.getResult().getAmount());
    }

    @Test
    void shapelessRecipe_missingRecipeSpec_throws() {
        // If no @RecipeSpec on the field, getAttachment throws
        // We use a config WITHOUT @RecipeSpec
        final class NoSpecConfig extends OkaeriConfig {
            public Recipe recipe = null;
        }

        assertThrows(Exception.class, () -> load(
                "recipe:\n  shapeless: true\n  shape:\n    - \"S\"\n  ingredients:\n    S: STONE\n  result:\n    material: DIAMOND\n    amount: 1",
                (Class) NoSpecConfig.class));
    }

    // ------------------------------------------------------------------ ShapedRecipe

    @Test
    void shapedRecipe_1x1() throws IOException {
        final ShapedConfig config = loadShaped("recipe:\n  shapeless: false\n  shape:\n    - \"A\"\n  ingredients:\n    A: DIAMOND\n  result:\n    material: STONE\n    amount: 1");

        assertInstanceOf(ShapedRecipe.class, config.recipe);
    }

    @Test
    void shapedRecipe_2x2() throws IOException {
        final ShapedConfig config = loadShaped("recipe:\n  shapeless: false\n  shape:\n    - \"AB\"\n    - \"CD\"\n  ingredients:\n    A: STONE\n    B: OAK_LOG\n    C: IRON_INGOT\n    D: GOLD_INGOT\n  result:\n    material: DIAMOND\n    amount: 1");

        assertInstanceOf(ShapedRecipe.class, config.recipe);
        final ShapedRecipe sr = (ShapedRecipe) config.recipe;
        assertEquals(2, sr.getShape().length);
    }

    @Test
    void shapedRecipe_result_material() throws IOException {
        final ShapedConfig config = loadShaped("recipe:\n  shapeless: false\n  shape:\n    - \"A\"\n  ingredients:\n    A: STONE\n  result:\n    material: GOLD_INGOT\n    amount: 1");

        assertEquals(Material.GOLD_INGOT, config.recipe.getResult().getType());
    }

    @Test
    void shapedRecipe_shape_isPreserved() throws IOException {
        final ShapedConfig config = loadShaped("recipe:\n  shapeless: false\n  shape:\n    - \"AAA\"\n    - \"ABA\"\n    - \"AAA\"\n  ingredients:\n    A: STONE\n    B: DIAMOND\n  result:\n    material: GOLD_INGOT\n    amount: 1");
        final ShapedRecipe sr = (ShapedRecipe) config.recipe;

        assertEquals(3, sr.getShape().length);
        assertEquals("AAA", sr.getShape()[0]);
    }

    // ------------------------------------------------------------------ FurnaceRecipe

    @Test
    void furnaceRecipe_ingredientAndResult() throws IOException {
        final FurnaceConfig config = load(
                "recipe:\n  ingredient: IRON_ORE\n  experience: 0.7\n  result:\n    material: IRON_INGOT\n    amount: 1",
                FurnaceConfig.class);

        assertNotNull(config.recipe);
        assertEquals(Material.IRON_INGOT, config.recipe.getResult().getType());
    }

    @Test
    void furnaceRecipe_experience() throws IOException {
        final FurnaceConfig config = load(
                "recipe:\n  ingredient: IRON_ORE\n  experience: 0.7\n  result:\n    material: IRON_INGOT\n    amount: 1",
                FurnaceConfig.class);

        assertEquals(0.7f, config.recipe.getExperience(), 0.01f);
    }

    @Test
    void furnaceRecipe_defaultCookingTime_noException() throws IOException {
        // cooking_time omitted → defaults to PT10S (200 ticks)
        assertDoesNotThrow(() -> load(
                "recipe:\n  ingredient: IRON_ORE\n  experience: 0.0\n  result:\n    material: IRON_INGOT\n    amount: 1",
                FurnaceConfig.class));
    }

    // ------------------------------------------------------------------ Parameterized

    @ParameterizedTest(name = "shapeless {0} ingredients")
    @ValueSource(ints = {1, 2, 3})
    void shapelessRecipe_ingredientCountMatches(int count) throws IOException {
        final StringBuilder shapeRow = new StringBuilder();
        final StringBuilder ingredients = new StringBuilder();
        for (int i = 0; i < count; i++) {
            final char key = (char) ('A' + i);
            shapeRow.append(key);
            ingredients.append("    ").append(key).append(": STONE\n");
        }
        final String yaml = "recipe:\n  shapeless: true\n  shape:\n    - \"" + shapeRow + "\"\n  ingredients:\n"
                + ingredients + "  result:\n    material: DIAMOND\n    amount: 1";
        final ShapelessConfig config = loadShapeless(yaml);

        assertNotNull(config.recipe);
        assertInstanceOf(ShapelessRecipe.class, config.recipe);
        assertEquals(count, ((ShapelessRecipe) config.recipe).getIngredientList().size());
    }
}
