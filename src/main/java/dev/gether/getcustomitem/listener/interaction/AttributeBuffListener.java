package dev.gether.getcustomitem.listener.interaction;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getcustomitem.item.customize.AttributeBuffItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AttributeBuffListener extends AbstractCustomItemListener<AttributeBuffItem> {

    public AttributeBuffListener(ItemManager itemManager,
                                 CooldownManager cooldownManager,
                                 FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        List<CustomItem> allCustomItemByType = GetCustomItem.getInstance().getItemManager().findAllCustomItemByType(ItemType.ATTRIBUTE_BUFF);
        if(allCustomItemByType.isEmpty()) return;

        allCustomItemByType.forEach(customItem -> {
            if(!(customItem instanceof AttributeBuffItem attributeBuffItem)) return;

            if(!attributeBuffItem.isResetAfterDeath())
                return;

            UUID buffUUID = UUID.nameUUIDFromBytes((attributeBuffItem.getItemID()).getBytes());
            for (Attribute attribute : Attribute.values()) {
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance == null) continue;

                for (AttributeModifier modifier : new ArrayList<>(instance.getModifiers())) {
                    if(modifier.getUniqueId().equals(buffUUID)) {
                        instance.removeModifier(modifier);
                    }
                }
            }
        });



    }
    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getCustomItem() instanceof AttributeBuffItem buffItem)) return;
        if(!buffItem.isEnabled()) return;

        Player player = event.getPlayer();
        if (!canUseItem(player, buffItem, event.getItemStack(), event.getEquipmentSlot())) return;

        applyPermanentBuffs(player, buffItem);

        buffItem.playSound(player.getLocation());
        buffItem.notifyYourself(player);
    }

    private void applyPermanentBuffs(Player player, AttributeBuffItem item) {
        UUID buffUUID = UUID.nameUUIDFromBytes((item.getItemID()).getBytes());

        item.getAttributes().forEach((attributeName, value) -> {
            try {
                Attribute attribute = Attribute.valueOf(attributeName);
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance == null) return;

                Optional<AttributeModifier> existingModifier = instance.getModifiers().stream()
                        .filter(mod -> mod.getUniqueId().equals(buffUUID))
                        .findFirst();

                double newValue = value;
                if (existingModifier.isPresent()) {
                    double oldValue = existingModifier.get().getAmount();
                    instance.removeModifier(existingModifier.get());

                    newValue += oldValue;
                }

                AttributeModifier modifier = new AttributeModifier(
                        buffUUID,
                        item.getItemID(),
                        newValue,
                        AttributeModifier.Operation.ADD_NUMBER
                );

                instance.addModifier(modifier);
            } catch (IllegalArgumentException ignored) {}
        });
    }
}