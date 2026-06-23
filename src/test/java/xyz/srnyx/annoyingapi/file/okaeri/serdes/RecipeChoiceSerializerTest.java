package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;
import org.bukkit.inventory.RecipeChoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.srnyx.annoyingapi.file.okaeri.MockBukkitTestSupport;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefRecipeChoice.RECIPE_CHOICE_CLASS;


/**
 * Tests for {@link xyz.srnyx.annoyingapi.file.okaeri.serdes.recipechoice.RecipeChoiceSerializer}.
 *
 * <p>RecipeChoice is a 1.13+ API. All tests are skipped if {@code RECIPE_CHOICE_CLASS == null}.
 * Requires MockBukkit for ItemStack construction (used by ExactChoice) and Material lookup.
 */
public class RecipeChoiceSerializerTest extends MockBukkitTestSupport {
    @TempDir Path tempDir;

    @BeforeEach
    void skipIfNoRecipeChoice() {
        assumeTrue(RECIPE_CHOICE_CLASS != null, "RecipeChoice not available — skipping on pre-1.13 runtime");
    }

    public static class TestConfig extends OkaeriConfig {
        public RecipeChoice choice = null;
    }

    private TestConfig load(String yaml) throws IOException {
        return loadFromYaml(tempDir, yaml, TestConfig.class);
    }

    // ------------------------------------------------------------------ MaterialChoice

    @Test
    void materialChoice_singleMaterial() throws IOException {
        final TestConfig config = load("choice:\n  type: MATERIAL\n  choices:\n    - STONE");

        assertNotNull(config.choice);
        assertInstanceOf(RecipeChoice.MaterialChoice.class, config.choice);
        final RecipeChoice.MaterialChoice mc = (RecipeChoice.MaterialChoice) config.choice;
        assertEquals(1, mc.getChoices().size());
        assertEquals(Material.STONE, mc.getChoices().get(0));
    }

    @Test
    void materialChoice_multipleMaterials() throws IOException {
        final TestConfig config = load("choice:\n  type: MATERIAL\n  choices:\n    - STONE\n    - COBBLESTONE\n    - GRAVEL");
        final RecipeChoice.MaterialChoice mc = (RecipeChoice.MaterialChoice) config.choice;

        assertEquals(3, mc.getChoices().size());
        assertTrue(mc.getChoices().contains(Material.STONE));
        assertTrue(mc.getChoices().contains(Material.COBBLESTONE));
        assertTrue(mc.getChoices().contains(Material.GRAVEL));
    }

    @Test
    void materialChoice_diamondAndGold() throws IOException {
        final TestConfig config = load("choice:\n  type: MATERIAL\n  choices:\n    - DIAMOND\n    - GOLD_INGOT");
        final RecipeChoice.MaterialChoice mc = (RecipeChoice.MaterialChoice) config.choice;

        assertEquals(2, mc.getChoices().size());
    }

    // ------------------------------------------------------------------ ExactChoice

    @Test
    void exactChoice_singleItemStack() throws IOException {
        final TestConfig config = load("choice:\n  type: EXACT\n  choices:\n    - material: DIAMOND\n      amount: 1");

        assertNotNull(config.choice);
        assertInstanceOf(RecipeChoice.ExactChoice.class, config.choice);
        final RecipeChoice.ExactChoice ec = (RecipeChoice.ExactChoice) config.choice;
        assertEquals(1, ec.getChoices().size());
        assertEquals(Material.DIAMOND, ec.getChoices().get(0).getType());
    }

    @Test
    void exactChoice_multipleItemStacks() throws IOException {
        final TestConfig config = load("choice:\n  type: EXACT\n  choices:\n    - material: DIAMOND\n      amount: 1\n    - material: GOLD_INGOT\n      amount: 1");
        final RecipeChoice.ExactChoice ec = (RecipeChoice.ExactChoice) config.choice;

        assertEquals(2, ec.getChoices().size());
    }

    // ------------------------------------------------------------------ Error paths

    @Test
    void missingTypeField_throws() {
        assertThrows(Exception.class, () -> load("choice:\n  choices:\n    - STONE"));
    }

    @Test
    void unknownType_throws() {
        assertThrows(Exception.class, () -> load("choice:\n  type: UNKNOWN_TYPE_XYZ\n  choices:\n    - STONE"));
    }

    // ------------------------------------------------------------------ Serialization round-trip

    @Test
    void materialChoice_roundTrip() throws IOException {
        final TestConfig first = load("choice:\n  type: MATERIAL\n  choices:\n    - STONE");

        assertInstanceOf(RecipeChoice.MaterialChoice.class, first.choice);
        assertEquals(Material.STONE, ((RecipeChoice.MaterialChoice) first.choice).getChoices().get(0));
    }

    @Test
    void exactChoice_roundTrip_materialPreserved() throws IOException {
        final TestConfig config = load("choice:\n  type: EXACT\n  choices:\n    - material: IRON_INGOT\n      amount: 1");
        final RecipeChoice.ExactChoice ec = (RecipeChoice.ExactChoice) config.choice;

        assertEquals(Material.IRON_INGOT, ec.getChoices().get(0).getType());
    }
}
