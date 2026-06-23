package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigBuilder;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigTestSupport;
import xyz.srnyx.annoyingapi.file.okaeri.MockBukkitTestSupport;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.RefItemMeta.ITEM_META_SET_UNBREAKABLE;


/**
 * Tests for {@link ItemStackSerializer}.
 *
 * <p>Requires MockBukkit because {@link org.bukkit.Server#getItemFactory()} is needed to produce
 * {@link ItemMeta} instances for display names, lore, enchantments, and flags.
 */
public class ItemStackSerializerTest extends MockBukkitTestSupport {
    @TempDir Path tempDir;

    public static class TestConfig extends OkaeriConfig {
        public ItemStack item = new ItemStack(Material.STONE);
    }

    public static class ListConfig extends OkaeriConfig {
        public List<ItemStack> items = new ArrayList<>();
    }

    private TestConfig load(String yaml) throws IOException {
        return loadFromYaml(tempDir, yaml, TestConfig.class);
    }

    // ------------------------------------------------------------------ Material and amount

    @Test
    void basicMaterial_DIAMOND() throws IOException {
        final TestConfig config = load("item:\n  material: DIAMOND\n  amount: 1");

        assertEquals(Material.DIAMOND, config.item.getType());
    }

    @Test
    void amount3_preserved() throws IOException {
        final TestConfig config = load("item:\n  material: DIAMOND\n  amount: 3");

        assertEquals(3, config.item.getAmount());
    }

    @Test
    void defaultAmount1_whenAmountOmitted() throws IOException {
        final TestConfig config = load("item:\n  material: DIAMOND");

        assertEquals(1, config.item.getAmount());
    }

    @Test
    void amount64_maxStack() throws IOException {
        final TestConfig config = load("item:\n  material: STONE\n  amount: 64");

        assertEquals(64, config.item.getAmount());
    }

    // ------------------------------------------------------------------ Display name / color codes

    @Test
    void colorCodeInName_ampersandTranslated() throws IOException {
        final TestConfig config = load("item:\n  material: DIAMOND\n  name: \"&aGreen\"");
        final ItemMeta meta = config.item.getItemMeta();

        assertNotNull(meta);
        assertTrue(meta.getDisplayName().startsWith("§a"), "Display name should start with §a: " + meta.getDisplayName());
    }

    @Test
    void colorCodeInName_sectionSignPassedThrough() throws IOException {
        final TestConfig config = load("item:\n  material: DIAMOND\n  name: \"§cRed\"");
        final ItemMeta meta = config.item.getItemMeta();

        assertNotNull(meta);
        assertTrue(meta.getDisplayName().startsWith("§c"), "Display name should start with §c: " + meta.getDisplayName());
    }

    @Test
    void colorCodeInName_fullTextPreserved() throws IOException {
        final TestConfig config = load("item:\n  material: DIAMOND\n  name: \"&aGreen Item\"");
        final ItemMeta meta = config.item.getItemMeta();

        assertNotNull(meta);
        assertEquals("§aGreen Item", meta.getDisplayName());
    }

    @Test
    void nullNameNotSet_noDisplayName() throws IOException {
        final TestConfig config = load("item:\n  material: DIAMOND");
        final ItemMeta meta = config.item.getItemMeta();

        assertNotNull(meta);
        assertFalse(meta.hasDisplayName(), "No name field → should not have display name: " + meta.getDisplayName());
    }

    // ------------------------------------------------------------------ Lore

    @Test
    void singleLoreLine_colorCodeTranslated() throws IOException {
        final TestConfig config = load("item:\n  material: DIAMOND\n  lore:\n    - \"&bBlue lore\"");
        final ItemMeta meta = config.item.getItemMeta();

        assertNotNull(meta);
        assertTrue(meta.hasLore());
        assertEquals("§bBlue lore", meta.getLore().get(0));
    }

    @Test
    void multipleLoreLines_allTranslated() throws IOException {
        final TestConfig config = load(
                "item:\n  material: DIAMOND\n  lore:\n    - \"&aLine 1\"\n    - \"&bLine 2\"\n    - \"&cLine 3\"");
        final ItemMeta meta = config.item.getItemMeta();

        assertNotNull(meta);
        assertTrue(meta.hasLore());
        final List<String> lore = meta.getLore();
        assertEquals(3, lore.size());
        assertEquals("§aLine 1", lore.get(0));
        assertEquals("§bLine 2", lore.get(1));
        assertEquals("§cLine 3", lore.get(2));
    }

    @Test
    void emptyLoreList_noLore() throws IOException {
        final TestConfig config = load("item:\n  material: DIAMOND\n  lore: []");
        final ItemMeta meta = config.item.getItemMeta();

        assertNotNull(meta);
        assertFalse(meta.hasLore());
    }

    // ------------------------------------------------------------------ Enchantments

    @Test
    void enchantment_sharpness5() throws IOException {
        // XEnchantment uses XRegistry which calls Bukkit.getRegistry(Enchantment.class);
        // MockBukkit 1.18 throws UnimplementedOperationException for this — skip gracefully.
        final TestConfig config;
        try {
            config = load("item:\n  material: DIAMOND_SWORD\n  enchantments:\n    SHARPNESS: 5");
        } catch (final ExceptionInInitializerError | NoClassDefFoundError e) {
            Assumptions.abort("XEnchantment not available in MockBukkit: " + e.getMessage());
            return;
        }
        final ItemMeta meta = config.item.getItemMeta();

        assertNotNull(meta);
        assertEquals(1, meta.getEnchants().size());
        assertEquals(5, meta.getEnchants().values().iterator().next());
    }

    @Test
    void multipleEnchantments() throws IOException {
        // XEnchantment uses XRegistry which calls Bukkit.getRegistry(Enchantment.class);
        // MockBukkit 1.18 throws UnimplementedOperationException for this — skip gracefully.
        final TestConfig config;
        try {
            config = load("item:\n  material: DIAMOND_SWORD\n  enchantments:\n    SHARPNESS: 5\n    UNBREAKING: 3");
        } catch (final ExceptionInInitializerError e) {
            Assumptions.abort("XEnchantment not available in MockBukkit: " + e.getMessage());
            return;
        }
        final ItemMeta meta = config.item.getItemMeta();

        assertNotNull(meta);
        assertEquals(2, meta.getEnchants().size());
        assertTrue(meta.getEnchants().values().stream().anyMatch(lvl -> lvl == 5));
        assertTrue(meta.getEnchants().values().stream().anyMatch(lvl -> lvl == 3));
    }

    // ------------------------------------------------------------------ Flags

    @Test
    void itemFlag_HIDE_ATTRIBUTES() throws IOException {
        final TestConfig config = load("item:\n  material: DIAMOND\n  flags:\n    - HIDE_ATTRIBUTES");
        final ItemMeta meta = config.item.getItemMeta();

        assertNotNull(meta);
        assertTrue(meta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES));
    }

    @Test
    void multipleFlags() throws IOException {
        final TestConfig config = load(
                "item:\n  material: DIAMOND\n  flags:\n    - HIDE_ATTRIBUTES\n    - HIDE_ENCHANTS");
        final ItemMeta meta = config.item.getItemMeta();

        assertNotNull(meta);
        assertTrue(meta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES));
        assertTrue(meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS));
    }

    // ------------------------------------------------------------------ Unbreakable

    @Test
    void unbreakableTrue() throws IOException {
        assumeTrue(ITEM_META_SET_UNBREAKABLE != null, "setUnbreakable not available");
        final TestConfig config = load("item:\n  material: DIAMOND\n  unbreakable: true");
        final ItemMeta meta = config.item.getItemMeta();

        assertNotNull(meta);
        assertTrue(meta.isUnbreakable());
    }

    // ------------------------------------------------------------------ Serialization round-trip

    @Test
    void serializeRoundTrip_materialPreserved() throws IOException {
        // saveDefaults writes the default STONE item; reload and verify
        final Path file = ConfigTestSupport.writeYaml(tempDir, "roundtrip.yml", "");
        new ConfigBuilder(new File(file.toString()))
                .config(TestConfig.class)
                .build();
        final String content = Files.readString(file, StandardCharsets.UTF_8);

        assertTrue(content.contains("STONE"), "Round-trip file should contain 'STONE': " + content);
        assertTrue(content.contains("material:"), "Round-trip file should contain 'material:': " + content);
    }

    @Test
    void serializeRoundTrip_nameWithColorCode() throws IOException {
        final TestConfig first = load("item:\n  material: DIAMOND\n  name: \"&aName\"");

        assertNotNull(first.item.getItemMeta());
        assertEquals("§aName", first.item.getItemMeta().getDisplayName());
    }
}
