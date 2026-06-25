package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttributeModifier;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttributeModifier.ATTRIBUTE_MODIFIER_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttributeModifier.ATTRIBUTE_MODIFIER_GET_NAME_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttributeModifier.ATTRIBUTE_MODIFIER_GET_OPERATION_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttributeModifier.ATTRIBUTE_MODIFIER_GET_SLOT_GROUP_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttributeModifier.ATTRIBUTE_MODIFIER_GET_SLOT_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttributeModifier.RefOperation.ATTRIBUTE_MODIFIER_OPERATION_ENUM;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefEquipmentSlotGroup.EQUIPMENT_SLOT_GROUP_ANY;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefEquipmentSlotGroup.EQUIPMENT_SLOT_GROUP_GET_BY_NAME_METHOD;


public class AttributeModifierSerializer implements ObjectSerializer<Object> {
    @Nullable private final Plugin plugin;

    public AttributeModifierSerializer(@Nullable Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean supports(@NotNull Class<?> type) {
        return ATTRIBUTE_MODIFIER_CLASS != null && ATTRIBUTE_MODIFIER_CLASS.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull Object object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        if (ATTRIBUTE_MODIFIER_GET_NAME_METHOD == null || ATTRIBUTE_MODIFIER_GET_OPERATION_METHOD == null) return;

        // name
        try {
            data.set("name", ATTRIBUTE_MODIFIER_GET_NAME_METHOD.invoke(object));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // operation
        try {
            data.set("operation", ATTRIBUTE_MODIFIER_GET_OPERATION_METHOD.invoke(object));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // slot
        try {
            if (ATTRIBUTE_MODIFIER_GET_SLOT_GROUP_METHOD != null) {
                // 1.20.5+
                data.set("slot", ATTRIBUTE_MODIFIER_GET_SLOT_GROUP_METHOD.invoke(object));
            } else if (ATTRIBUTE_MODIFIER_GET_SLOT_METHOD != null) {
                // 1.20.4-
                data.set("slot", ATTRIBUTE_MODIFIER_GET_SLOT_METHOD.invoke(object));
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override @NotNull
    public Object deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        // name
        final String name = data.get("name", String.class);
        if (name == null) throw new IllegalArgumentException("Missing required field: name");

        // amount
        final Double amount = data.get("amount", Double.class);
        if (amount == null) throw new IllegalArgumentException("Missing required field: amount");

        // operation
        final Enum<?> operation = data.get("operation", ATTRIBUTE_MODIFIER_OPERATION_ENUM);
        if (operation == null) throw new IllegalArgumentException("Missing required field: operation");

        // slot
        Object slot;
        if (EQUIPMENT_SLOT_GROUP_ANY != null && EQUIPMENT_SLOT_GROUP_GET_BY_NAME_METHOD != null) {
            // 1.20.5+
            slot = EQUIPMENT_SLOT_GROUP_ANY;
            final String slotName = data.get("slot", String.class);
            if (slotName != null) try {
                slot = EQUIPMENT_SLOT_GROUP_GET_BY_NAME_METHOD.invoke(null, slotName);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        } else {
            // 1.20.4-
            slot = data.get("slot", EquipmentSlot.class);
        }
        if (slot == null) throw new IllegalArgumentException("Missing required field: slot");

        // Return constructed AttributeModifier
        final Object attributeModifier = RefAttributeModifier.constructAttributeModifier(plugin, name, amount, operation, slot);
        if (attributeModifier == null) throw new IllegalStateException("Failed to construct AttributeModifier");
        return attributeModifier;
    }
}
