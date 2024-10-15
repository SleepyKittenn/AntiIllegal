package live.amsleepy.antiillegalbukkit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AntiIllegalListener implements Listener {

    private final AntiIllegal_Bukkit plugin;

    public AntiIllegalListener(AntiIllegal_Bukkit plugin) {
        this.plugin = plugin;
    }

    private void handleItem(ItemStack item, Player player, String action) {
        if (item == null) {
            // plugin.getLogger().info(plugin.getPrefix() + "No item to handle for action: " + action);
            return;
        }

        // plugin.getLogger().info(plugin.getPrefix() + "Handling item: " + item.getType().name() + " for action: " + action);

        List<String> monitoredItems = plugin.config.getStringList("monitored_items");
        if (!monitoredItems.contains(item.getType().name())) {
            // plugin.getLogger().info(plugin.getPrefix() + "Item " + item.getType().name() + " is not monitored.");
            return;
        }

        if (isItemExempt(item)) {
            // plugin.getLogger().info(plugin.getPrefix() + "Item is exempt based on lore.");
            return;
        }

        Map<Enchantment, Integer> itemEnchants = item.getEnchantments();
        boolean isIllegal = false;
        StringBuilder logEntry = new StringBuilder();

        for (Map.Entry<Enchantment, Integer> entry : itemEnchants.entrySet()) {
            String enchantName = entry.getKey().getKey().getKey();
            int enchantLevel = entry.getValue();
            int maxLevel = plugin.config.getInt("monitored_enchantments." + enchantName, -1);

            // plugin.getLogger().info(plugin.getPrefix() + "Checking enchantment: " + enchantName + " level " + enchantLevel + " against max " + maxLevel);

            if (maxLevel != -1 && enchantLevel > maxLevel) {
                item.addUnsafeEnchantment(entry.getKey(), maxLevel);
                isIllegal = true;
                logEntry.append(String.format("%s: %d -> %d; ", enchantName, enchantLevel, maxLevel));
            }
        }

        if (isIllegal) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
            String ip = (player.getAddress() != null) ? player.getAddress().getAddress().getHostAddress() : "unknown";
            String username = player.getName();
            String world = player.getWorld().getName();
            String coords = player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ();
            String itemName = item.getType().name();

            String fullLogEntry = String.format("%s - [%s] %s@%s %s: %s [%s]", time, ip, username, world, coords, action, itemName);
            logEntry.insert(0, fullLogEntry);

            plugin.logItemRevert(logEntry.toString());
        } else {
            // plugin.getLogger().info(plugin.getPrefix() + "No illegal enchantments found on " + item.getType().name());
        }
    }

    private boolean isItemExempt(ItemStack item) {
        List<String> exemptLores = plugin.config.getStringList("exempt_item_lore");
        exemptLores = exemptLores.stream()
                .map(lore -> ChatColor.translateAlternateColorCodes('&', lore))
                .collect(Collectors.toList());

        plugin.getLogger().info(plugin.getPrefix() + "Translated exempt lores from config: " + exemptLores);

        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> itemLores = item.getItemMeta().getLore();
            plugin.getLogger().info(plugin.getPrefix() + "Item lores: " + itemLores);

            for (String itemLore : itemLores) {
                if (exemptLores.contains(itemLore)) {
                    plugin.getLogger().info(plugin.getPrefix() + "Item is exempt based on lore: " + itemLore);
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        handleItem(event.getPlayer().getInventory().getItem(event.getNewSlot()), event.getPlayer(), "held an item");
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        handleItem(event.getItemDrop().getItemStack(), event.getPlayer(), "dropped an item");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        handleItem(event.getItem(), event.getPlayer(), "interacted with an item");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        handleItem(event.getEntity().getInventory().getItemInMainHand(), event.getEntity(), "died with an item");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            handleItem(event.getCurrentItem(), player, "placed in inventory");
            handleItem(event.getCursor(), player, "placed in inventory");

            switch (event.getSlotType()) {
                case ARMOR:
                case QUICKBAR:
                    handleItem(event.getCurrentItem(), player, "armor equip/unequip");
                    handleItem(event.getCursor(), player, "armor equip/unequip");
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            for (ItemStack item : event.getNewItems().values()) {
                handleItem(item, player, "dragged in inventory");
            }
        }
    }

    @EventHandler
    public void onBlockDispenseArmor(BlockDispenseArmorEvent event) {
        if (event.getTargetEntity() instanceof Player) {
            handleItem(event.getItem(), (Player) event.getTargetEntity(), "dispensed armor onto player");
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            handleItem(event.getItem().getItemStack(), (Player) event.getEntity(), "picked up an item");
        }
    }
}