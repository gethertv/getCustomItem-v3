package dev.gether.getcustomitem.listener.damage;

import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.armor.ArmorDegraderItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;


public class ArmorDegraderListener extends AbstractCustomItemListener<ArmorDegraderItem> {

    private final Random random = new Random();

    public ArmorDegraderListener(ItemManager itemManager,
                                CooldownManager cooldownManager,
                                FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCustomItemDamage(CustomItemDamageEvent event) {
        if(event.isCancelled()) return;
        
        if(!(event.getCustomItem() instanceof ArmorDegraderItem armorDegraderItem)) return;
        if(!armorDegraderItem.isEnabled()) return;
        
        Player damager = event.getDamager();

        if (!canUseItem(damager, armorDegraderItem, event.getItemStack(), event.getEquipmentSlot())) return;
        
        EntityEquipment victimEquipment = event.getVictim().getEquipment();
        if (victimEquipment == null) return;
        
        Set<EquipmentSlot> targetSlots = armorDegraderItem.getTargetEquipmentSlots();
        
        if (targetSlots == null || targetSlots.isEmpty()) {
            return;
        }
        
        boolean damaged = degradeVictimArmor(victimEquipment, armorDegraderItem.getDurabilityDamagePercent(), targetSlots);
        
        if (damaged) {
            armorDegraderItem.playSound(damager.getLocation());
            armorDegraderItem.notifyYourself(damager);
            armorDegraderItem.notifyOpponents(damager);
        }
        
        event.setCancelEvent(false);
    }

    private boolean degradeVictimArmor(EntityEquipment equipment, double damagePercent, Set<EquipmentSlot> targetSlots) {
        boolean anyArmorDamaged = false;
        
        List<EquipmentSlot> availableSlots = new ArrayList<>(targetSlots);
        
        if (availableSlots.isEmpty()) {
            return false;
        }
        
        int partsToDegrade = availableSlots.size() > 1 ? random.nextInt(2) + 1 : 1;
        partsToDegrade = Math.min(partsToDegrade, availableSlots.size());
        
        Collections.shuffle(availableSlots);
        
        for (int i = 0; i < partsToDegrade; i++) {
            EquipmentSlot slot = availableSlots.get(i);
            ItemStack armorPiece = getArmorItemInSlot(equipment, slot);
            
            if (armorPiece != null && !armorPiece.getType().isAir() && armorPiece.getItemMeta() instanceof Damageable) {
                if (degradeArmorPiece(armorPiece, damagePercent)) {
                    updateArmorInSlot(equipment, slot, armorPiece);
                    anyArmorDamaged = true;
                }
            }
        }
        
        return anyArmorDamaged;
    }
    
    private ItemStack getArmorItemInSlot(EntityEquipment equipment, EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> equipment.getHelmet();
            case CHEST -> equipment.getChestplate();
            case LEGS -> equipment.getLeggings();
            case FEET -> equipment.getBoots();
            default -> null;
        };
    }
    
    private void updateArmorInSlot(EntityEquipment equipment, EquipmentSlot slot, ItemStack armorPiece) {
        switch (slot) {
            case HEAD -> equipment.setHelmet(armorPiece);
            case CHEST -> equipment.setChestplate(armorPiece);
            case LEGS -> equipment.setLeggings(armorPiece);
            case FEET -> equipment.setBoots(armorPiece);
            default -> {}
        }
    }
    
    private boolean degradeArmorPiece(ItemStack armorPiece, double damagePercent) {
        ItemMeta meta = armorPiece.getItemMeta();
        if (!(meta instanceof Damageable damageable)) return false;
        
        int maxDurability = armorPiece.getType().getMaxDurability();
        if (maxDurability <= 0 || meta.isUnbreakable()) return false;
        int currentDurability = maxDurability - damageable.getDamage();
        int durabilityToRemove = (int) Math.ceil(maxDurability * (damagePercent / 100.0));
        int newDamage = damageable.getDamage() + durabilityToRemove;
        if (newDamage >= maxDurability) {
            newDamage = maxDurability - 1;
        }
        damageable.setDamage(newDamage);
        armorPiece.setItemMeta(meta);
        
        return true;
    }
}