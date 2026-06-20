package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import com.google.common.collect.Multimap;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.utility.BukkitUtility;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttribute.ATTRIBUTE_ENUM;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttributeModifier.ATTRIBUTE_MODIFIER_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.RefDamageable.DAMAGEABLE_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.RefDamageable.DAMAGEABLE_GET_DAMAGE_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.RefDamageable.DAMAGEABLE_SET_DAMAGE_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.RefItemMeta.*;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.RefItemMeta.ITEM_META_SET_UNBREAKABLE;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.components.RefCustomModelDataComponent.*;


public class ItemStackSerializer implements ObjectSerializer<ItemStack> {
    @Override
    public boolean supports(@NotNull Class<?> type) {
        return ItemStack.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull ItemStack object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        // material, amount
        data.set("material", object.getType().name());
        data.set("amount", object.getAmount());

        final ItemMeta meta = object.getItemMeta();
        final boolean hasMeta = meta != null;

        // durability
        if (DAMAGEABLE_CLASS != null && DAMAGEABLE_GET_DAMAGE_METHOD != null) {
            // 1.13+
            if (hasMeta) try {
                data.set("durability", DAMAGEABLE_GET_DAMAGE_METHOD.invoke(meta));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        } else {
            // 1.12.2
            data.set("durability", object.getDurability());
        }

        // Meta stuff
        if (hasMeta) {
            // name
            if (meta.hasDisplayName()) data.set("name", meta.getDisplayName());

            // lore
            if (meta.hasLore()) data.set("lore", meta.getLore());

            // enchantments
            if (!meta.getEnchants().isEmpty()) data.set("enchantments", meta.getEnchants());

            // flags
            if (!meta.getItemFlags().isEmpty()) data.set("flags", meta.getItemFlags());

            // 1.11+ unbreakable
            if (ITEM_META_IS_UNBREAKABLE != null) try {
                if ((boolean) ITEM_META_IS_UNBREAKABLE.invoke(meta)) data.set("unbreakable", true);
            } catch (final Exception e) {
                e.printStackTrace();
            }

            // 1.13.2+ attribute-modifiers
            if (ITEM_META_GET_ATTRIBUTE_MODIFIERS != null) {
                try {
                    final Multimap<Object, Object> attributeModifiers = (Multimap<Object, Object>) ITEM_META_GET_ATTRIBUTE_MODIFIERS.invoke(meta);
                    for (final Object attribute : attributeModifiers.keySet()) {
                        final Collection<?> modifiers = attributeModifiers.get(attribute);
                        if (!modifiers.isEmpty()) data.set("attribute-modifiers." + attribute, modifiers.iterator().next());
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }

            // 1.14+ custom-model-data
            if (ITEM_META_HAS_CUSTOM_MODEL_DATA != null && ITEM_META_GET_CUSTOM_MODEL_DATA != null) try {
                if ((boolean) ITEM_META_HAS_CUSTOM_MODEL_DATA.invoke(meta)) data.set("custom-model-data", ITEM_META_GET_CUSTOM_MODEL_DATA.invoke(meta));
            } catch (final Exception e) {
                e.printStackTrace();
            }

            // 1.21.4+ custom-model-data-component
            if (ITEM_META_GET_CUSTOM_MODEL_DATA_COMPONENT != null) try {
                final Object component = ITEM_META_GET_CUSTOM_MODEL_DATA_COMPONENT.invoke(meta);

                // colors
                if (CUSTOM_MODEL_DATA_COMPONENT_GET_COLORS_METHOD != null) try {
                    final List<?> colors = (List<?>) CUSTOM_MODEL_DATA_COMPONENT_GET_COLORS_METHOD.invoke(component);
                    if (!colors.isEmpty()) data.set("custom-model-data-components.colors", colors);
                } catch (final Exception e) {
                    e.printStackTrace();
                }

                // flags
                if (CUSTOM_MODEL_DATA_COMPONENT_GET_FLAGS_METHOD != null) try {
                    final List<?> flags = (List<?>) CUSTOM_MODEL_DATA_COMPONENT_GET_FLAGS_METHOD.invoke(component);
                    if (!flags.isEmpty()) data.set("custom-model-data-components.flags", flags);
                } catch (final Exception e) {
                    e.printStackTrace();
                }

                // floats
                if (CUSTOM_MODEL_DATA_COMPONENT_GET_FLAGS_METHOD != null) try {
                    final List<?> floats = (List<?>) CUSTOM_MODEL_DATA_COMPONENT_GET_FLAGS_METHOD.invoke(component);
                    if (!floats.isEmpty()) data.set("custom-model-data-components.floats", floats);
                } catch (final Exception e) {
                    e.printStackTrace();
                }

                // strings
                if (CUSTOM_MODEL_DATA_COMPONENT_GET_STRINGS_METHOD != null) try {
                    final List<?> strings = (List<?>) CUSTOM_MODEL_DATA_COMPONENT_GET_STRINGS_METHOD.invoke(component);
                    if (!strings.isEmpty()) data.set("custom-model-data-components.strings", strings);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override @NotNull
    public ItemStack deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        // material
        final Material material = data.get("material", Material.class);

        // amount -> 1
        final int amount = data.getOr("amount", int.class, 1);

        // durability -> damage -> 0
        final int durability = data.getOr("durability", int.class, data.getOr("damage", int.class, 0));

        // Create ItemStack
        final boolean useDamageable = DAMAGEABLE_CLASS != null && DAMAGEABLE_SET_DAMAGE_METHOD != null;
        final ItemStack item = useDamageable ? new ItemStack(material, amount) : new ItemStack(material, amount, (short) durability);

        // Meta stuff
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // name, lore TODO: color stuff might be removed when re-serializing. might need custom ItemStackWrapper that keeps raw values stored. and switch to minimessage?
            meta.setDisplayName(BukkitUtility.color(data.get("name", String.class)));
            meta.setLore(BukkitUtility.colorCollection(data.getAsList("lore", String.class)));

            // enchantments
            for (final Map.Entry<Enchantment, Integer> enchantment : data.getAsMap("enchantments", Enchantment.class, int.class).entrySet()) {
                meta.addEnchant(enchantment.getKey(), enchantment.getValue(), true);
            }

            // flags
            meta.addItemFlags(data.getAsList("flags", ItemFlag.class).toArray(new ItemFlag[0]));

            // 1.11+ unbreakable
            if (ITEM_META_SET_UNBREAKABLE != null) try {
                ITEM_META_SET_UNBREAKABLE.invoke(meta, data.get("unbreakable", boolean.class));
            } catch (final IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            // 1.13.2+ attribute-modifiers
            if (ATTRIBUTE_ENUM != null && ITEM_META_ADD_ATTRIBUTE_MODIFIER != null) {
                for (final Map.Entry<? extends Enum, ?> entry : data.getAsMap("attribute-modifiers", ATTRIBUTE_ENUM, ATTRIBUTE_MODIFIER_CLASS).entrySet()) {
                    final Enum attribute = entry.getKey();
                    if (attribute == null) continue;
                    final Object modifier = entry.getValue();
                    if (modifier == null) continue;
                    try {
                        ITEM_META_ADD_ATTRIBUTE_MODIFIER.invoke(meta, attribute, modifier);
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }

            // 1.14+ (custom model data)
            if (ITEM_META_SET_CUSTOM_MODEL_DATA != null) {
                final int customModelData = data.getOr("custom-model-data", int.class, 0);
                if (customModelData != 0) try {
                    ITEM_META_SET_CUSTOM_MODEL_DATA.invoke(meta, customModelData);
                } catch (final IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            // 1.21.4+ custom-model-data-component
            if (ITEM_META_GET_CUSTOM_MODEL_DATA_COMPONENT != null && ITEM_META_SET_CUSTOM_MODEL_DATA_COMPONENT != null) try {
                // Get component
                final Object component = ITEM_META_GET_CUSTOM_MODEL_DATA_COMPONENT.invoke(meta);

                // colors
                if (CUSTOM_MODEL_DATA_COMPONENT_SET_COLORS_METHOD != null) {
                    final List<?> colors = data.getAsList("custom-model-data-components.colors", Color.class);
                    if (!colors.isEmpty()) try {
                        CUSTOM_MODEL_DATA_COMPONENT_SET_COLORS_METHOD.invoke(component, colors);
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

                // flags
                if (CUSTOM_MODEL_DATA_COMPONENT_SET_FLAGS_METHOD != null) {
                    final List<?> flags = data.getAsList("custom-model-data-components.flags", String.class);
                    if (!flags.isEmpty()) try {
                        CUSTOM_MODEL_DATA_COMPONENT_SET_FLAGS_METHOD.invoke(component, flags);
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

                // floats
                if (CUSTOM_MODEL_DATA_COMPONENT_SET_FLOATS_METHOD != null) {
                    final List<?> floats = data.getAsList("custom-model-data-components.floats", float.class);
                    if (!floats.isEmpty()) try {
                        CUSTOM_MODEL_DATA_COMPONENT_SET_FLOATS_METHOD.invoke(component, floats);
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

                // strings
                if (CUSTOM_MODEL_DATA_COMPONENT_SET_STRINGS_METHOD != null) {
                    final List<?> strings = data.getAsList("custom-model-data-components.strings", String.class);
                    if (!strings.isEmpty()) try {
                        CUSTOM_MODEL_DATA_COMPONENT_SET_STRINGS_METHOD.invoke(component, strings);
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

                // Set component
                ITEM_META_SET_CUSTOM_MODEL_DATA_COMPONENT.invoke(meta, component);
            } catch (final IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            // Set meta
            item.setItemMeta(meta);
        }

        return item;
    }
}
