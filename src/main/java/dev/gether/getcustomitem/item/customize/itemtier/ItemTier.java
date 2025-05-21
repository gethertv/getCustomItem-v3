package dev.gether.getcustomitem.item.customize.itemtier;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getechantingitem.EnchantingItem;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.getutils.utils.ColorFixer;
import dev.gether.getutils.utils.ItemUtil;
import dev.gether.getutils.utils.NumberUtils;
import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

@Getter
@Setter
@JsonTypeName("item_tier")
@SuperBuilder
@NoArgsConstructor
public class ItemTier extends CustomItem {

    private static final String ITEM_TIER_KEY = "itemtier";
    private static final String ITEM_TIER_PROGRESS_KEY = "itemtier-progress";

    private NamespacedKey ITEM_KEY;
    private NamespacedKey ITEM_TIER;
    private NamespacedKey ITEM_TIER_PROGRESS;
    private Map<Integer, TierData> tierData;
    private Set<EquipmentSlot> equipmentSlots;

    @JsonIgnore
    private transient ItemStack itemTier;

    private String charProgress;
    private int lengthProgress;
    private String successColor;
    private String failureColor;

    @Override
    protected Map<String, String> replacementValues() {
        return Collections.emptyMap();
    }

    @Override
    public void init() {
        super.init();
        initNamespacedKeys();
        initItemTier();
        initTierData();
    }

    private void initNamespacedKeys() {
        this.ITEM_KEY = new NamespacedKey(GetCustomItem.getInstance(), getItemID());
        this.ITEM_TIER = new NamespacedKey(GetCustomItem.getInstance(), ITEM_TIER_KEY);
        this.ITEM_TIER_PROGRESS = new NamespacedKey(GetCustomItem.getInstance(), ITEM_TIER_PROGRESS_KEY);
    }

    private void initItemTier() {
        itemTier = super.getItemStack();
        ItemMeta itemMeta = itemTier.getItemMeta();
        if (itemMeta != null) {
            itemMeta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.BYTE, (byte)1);
            itemMeta.getPersistentDataContainer().set(ITEM_TIER, PersistentDataType.INTEGER, 0);
            itemMeta.getPersistentDataContainer().set(ITEM_TIER_PROGRESS, PersistentDataType.DOUBLE, 0.0);
            itemTier.setItemMeta(itemMeta);
        }
    }

    private void initTierData() {
        tierData.values().forEach(tier -> tier.init(ITEM_TIER, ITEM_TIER_PROGRESS));
    }

    @JsonIgnore
    public Optional<Integer> getLevel(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        return itemMeta != null ? Optional.ofNullable(itemMeta.getPersistentDataContainer().get(ITEM_TIER, PersistentDataType.INTEGER)) : Optional.empty();
    }

    @JsonIgnore
    public boolean isItemTier(ItemStack item) {
        if(item == null) return false;
        if(!item.hasItemMeta()) return false;

        return item.getItemMeta().getPersistentDataContainer().has(ITEM_KEY, PersistentDataType.BYTE);

    }

    @JsonIgnore
    public ItemStack getItemTier() {
        return itemTier;
    }

    @JsonIgnore
    public ItemStack getItemStack() {
        ItemStack clone = itemTier.clone();
        ItemMeta itemMeta = clone.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setCustomModelData(GetCustomItem.getRandom().nextInt());
            clone.setItemMeta(itemMeta);
        }
        updateItem(clone);
        return clone;
    }

    @JsonIgnore
    private String getProgress(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return null;

        Optional<Integer> levelOptional = getLevel(itemStack);
        if(levelOptional.isEmpty())
            return "";

        int level = levelOptional.get() + 1;
        TierData tierData = this.tierData.get(level);
        if(tierData==null) return "";

        Double progress = itemMeta.getPersistentDataContainer().get(ITEM_TIER_PROGRESS, PersistentDataType.DOUBLE);
        double result = progress / tierData.getRequirementValue();

        int successCount = (int) (result * lengthProgress);
        int failureCount = lengthProgress - successCount;

        StringBuilder progressBar = new StringBuilder();

        for (int i = 0; i < successCount; i++) {
            progressBar.append(successColor).append(charProgress);
        }

        for (int i = 0; i < failureCount; i++) {
            progressBar.append(failureColor).append(charProgress);
        }

        return progressBar.toString();
    }

    @JsonIgnore
    @Override
    public void updateItem(ItemStack itemStack) {
        ItemStack old = itemStack.clone();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;

        Integer usage = itemMeta.getPersistentDataContainer().get(getUsageKey(), PersistentDataType.INTEGER);
        if (usage == null) return;

        Optional<Integer> levelOptional = getLevel(itemStack);
        if (levelOptional.isEmpty()) return;

        int level = levelOptional.get();
        TierData nextTierData = this.tierData.get(level + 1);
        List<String> lore = getLoreForLevel(level);

        if (lore.isEmpty()) return;

        Double hasAmount = itemMeta.getPersistentDataContainer().get(ITEM_TIER_PROGRESS, PersistentDataType.DOUBLE);
        lore = updateLore(lore, usage, level, hasAmount, (nextTierData != null ?  nextTierData.getRequirementValue() : 0d), getProgress(itemStack));

        itemMeta.setLore(ColorFixer.addColors(lore));
        itemStack.setItemMeta(itemMeta);
        EnchantingItem enchantingItem = GetCustomItem.getInstance().getHookManager().getEnchantingItem();
        if(enchantingItem != null) {
            enchantingItem.getAverageDamageManager().update(old, itemStack);
        }
    }

    @JsonIgnore
    private List<String> getLoreForLevel(int level) {
        if (level == 0) {
            return new ArrayList<>(super.getItem().getLore());
        } else {
            TierData tierData = this.tierData.get(level);
            return new ArrayList<>(tierData.getItem().getLore());
        }
    }

    @JsonIgnore
    private List<String> updateLore(List<String> lore, int usage, int level, Double hasAmount, double needAmount, String progress) {
        return new ArrayList<>(
                lore.stream()
                        .map(line -> line.replace("{requirement-int}", String.format(Locale.US, "%d", hasAmount.intValue()))
                                .replace("{requirement-double}", String.format(Locale.US, "%.2f", hasAmount))
                                .replace("{need-int}", String.format(Locale.US, "%d", (int) needAmount))
                                .replace("{need-double}", String.format(Locale.US, "%.2f", needAmount))
                                .replace("{progress}", progress)
                                .replace("{percent}", String.format(Locale.US, "%.2f", (hasAmount/needAmount)*100))
                                .replace("{level}", String.valueOf(level))
                                .replace("{level-increase}", String.valueOf(level + 1))
                                .replace("{level-decrease}", String.valueOf(level - 1))
                                .replace("{level-roman}", NumberUtils.intToRoman(level))
                                .replace("{level-roman-increase}", NumberUtils.intToRoman(level + 1))
                                .replace("{level-roman-decrease}", NumberUtils.intToRoman(level - 1)))
                        .toList()
        );
    }

    public void action(Player player, ActionEvent actionEvent, Object type, int amount, ItemStack itemStack) {
        ItemStack copy = itemStack.clone();
        Optional<Integer> levelOptional = getLevel(itemStack);
        if (levelOptional.isEmpty()) return;

        int level = levelOptional.get() + 1;
        TierData tierData = this.tierData.get(level);
        if (tierData == null) return;

        Map<Object, Double> objectDoubleMap = tierData.getActionEvents().get(actionEvent);
        if (objectDoubleMap == null) return;

        Double amountToAdd = objectDoubleMap.get(type.toString());
        if (amountToAdd == null) return;

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;

        Double actuallyProgress = itemMeta.getPersistentDataContainer().get(ITEM_TIER_PROGRESS, PersistentDataType.DOUBLE);
        if (actuallyProgress == null) {
            actuallyProgress = 0.0;
        }
        actuallyProgress += (amountToAdd * amount);

        if (actuallyProgress > tierData.getRequirementValue()) {
            double rest = actuallyProgress - tierData.getRequirementValue();
            upgradeItem(player, rest, level, itemStack, copy);
        } else {
            itemMeta.getPersistentDataContainer().set(ITEM_TIER_PROGRESS, PersistentDataType.DOUBLE, actuallyProgress);
            itemStack.setItemMeta(itemMeta);
            updateItem(itemStack);
        }
    }

    private void upgradeItem(Player player, double rest, int level, ItemStack itemStack, ItemStack clone) {
        Integer usage = itemStack.getItemMeta().getPersistentDataContainer().get(getUsageKey(), PersistentDataType.INTEGER);
        int slot = ItemUtil.removeItemReturnSlot(player, itemStack, 1);
        TierData tierDataItem = this.tierData.get(level);
        ItemStack item = tierDataItem.getItemStack().clone();
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.BYTE, (byte) 1);
            itemMeta.getPersistentDataContainer().set(ITEM_TIER_PROGRESS, PersistentDataType.DOUBLE, rest);
            itemMeta.getPersistentDataContainer().set(ITEM_TIER, PersistentDataType.INTEGER, level);
            itemMeta.getPersistentDataContainer().set(getUsageKey(), PersistentDataType.INTEGER, usage);
            item.setItemMeta(itemMeta);
        }
        updateItem(item);

        EnchantingItem enchantingItem = GetCustomItem.getInstance().getHookManager().getEnchantingItem();
        if(enchantingItem != null) {
            enchantingItem.getAverageDamageManager().update(clone, item);
        }

        player.getInventory().setItem(slot, item);
    }

    public boolean isMaxLevel(ItemStack itemStack) {
        Optional<Integer> level = getLevel(itemStack);
        if (level.isEmpty())
            return true;
        Integer lvl = level.get();
        TierData tierData = this.tierData.get(Integer.valueOf(lvl.intValue() + 1));
        if (tierData == null)
            return true;
        return false;
    }

    @JsonIgnore
    public static ItemTier createDefaultItem() {
        return ItemTier.builder()
                .itemID("excalibur_tier")
                .categoryName("excalibur_tier_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.DIAMOND_SWORD)
                        .name("#f2ff69Excalibur")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "&7need-int: {need-int}",
                                        "&7need-double: {need-double}",
                                        "&7requirement-int: {requirement-int}",
                                        "&7requirement-double: {requirement-double}",
                                        "&7percent: {percent}%",
                                        "&7Level: {level}",
                                        "&7Next-level: {level-increase}",
                                        "&7Previous: {level-decrease}",
                                        "&7Roman level: {level-roman}",
                                        "&7Roman next-level: {level-roman-increase}",
                                        "&7Roman previous-level: {level-roman-decrease}",
                                        "&7",
                                        "&7{progress}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(false)
                        .build())
                .itemType(ItemType.ITEM_TIER)
                .cooldown(5)
                .permissionBypass("getcustomitem.excaliburtier.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .equipmentSlots(new HashSet<>(Set.of(
                        EquipmentSlot.HAND
                )))
                .tierData(new HashMap<>(Map.of(
                        1, TierData.builder()
                                .item(Item.builder()
                                        .amount(1)
                                        .material(Material.NETHERITE_SWORD)
                                        .name("#f2ff69Excalibur")
                                        .lore(new ArrayList<>(
                                                List.of(
                                                        "&7",
                                                        "&7need-int: {need-int}",
                                                        "&7need-double: {need-double}",
                                                        "&7requirement-int: {requirement-int}",
                                                        "&7requirement-double: {requirement-double}",
                                                        "&7percent: {percent}",
                                                        "&7Level: {level}",
                                                        "&7Next-level: {level-increase}",
                                                        "&7Previous: {level-decrease}",
                                                        "&7Roman level: {level-roman}",
                                                        "&7Roman next-level: {level-roman-increase}",
                                                        "&7Roman previous-level: {level-roman-decrease}",
                                                        "&7",
                                                        "&7{progress}",
                                                        "&7"
                                                )
                                        ))
                                        .enchantments(Map.of(
                                                Enchantment.KNOCKBACK, 1
                                        ))
                                        .attributeModifiers(new HashMap<>(Map.of(
                                                Attribute.GENERIC_LUCK, new ArrayList<>(List.of(
                                                        new AttributeModifier(UUID.randomUUID(), "excalibur", 2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND)
                                                ))
                                        )))
                                        .unbreakable(true)
                                        .glow(false)
                                        .build())
                                .requirementValue(10)
                                .actionEvents(new HashMap<>(
                                        Map.of(ActionEvent.KILL_ENTITY, new HashMap<>(
                                                Map.of(EntityType.ZOMBIE, 1.0)
                                        ))
                                ))
                                .build()
                )))
                .charProgress("-")
                .lengthProgress(10)
                .successColor("#7cff3b")
                .failureColor("#636363")
                .build();

    }
}