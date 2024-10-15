package live.amsleepy.antiillegalbukkit;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.util.*;
import java.util.stream.Collectors;

public class AntiIllegalCommandExecutor implements CommandExecutor, TabCompleter {

    private final AntiIllegal_Bukkit plugin;

    public AntiIllegalCommandExecutor(AntiIllegal_Bukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getPrefix() + "Usage: /antiillegal <reload|lore|item|enchantments> [...]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (sender.hasPermission("antiillegal.reload")) {
                    plugin.reloadConfig();
                    plugin.config = plugin.getConfig();
                    sender.sendMessage(plugin.getPrefix() + "AntiIllegal configuration reloaded successfully.");
                } else {
                    sender.sendMessage(plugin.getPrefix() + "You do not have permission to use this command.");
                }
                break;

            case "lore":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getPrefix() + "Usage: /antiillegal lore <exempt|unexempt|list> [<lore>]");
                } else if (sender.hasPermission("antiillegal.lore")) {
                    handleLoreCommand(sender, args);
                } else {
                    sender.sendMessage(plugin.getPrefix() + "You do not have permission to use this command.");
                }
                break;

            case "item":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getPrefix() + "Usage: /antiillegal item <monitor|unmonitor> <item>");
                } else if (sender.hasPermission("antiillegal.item")) {
                    handleItemCommand(sender, args);
                } else {
                    sender.sendMessage(plugin.getPrefix() + "You do not have permission to use this command.");
                }
                break;

            case "enchantments":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getPrefix() + "Usage: /antiillegal enchantments <monitor|unmonitor> <enchantment> [maxlevel]");
                } else if (sender.hasPermission("antiillegal.enchantments")) {
                    handleEnchantmentCommand(sender, args);
                } else {
                    sender.sendMessage(plugin.getPrefix() + "You do not have permission to use this command.");
                }
                break;

            default:
                sender.sendMessage(plugin.getPrefix() + "Invalid command. Usage: /antiillegal <reload|lore|item|enchantments> [...]");
                break;
        }
        return true;
    }

    private void handleLoreCommand(CommandSender sender, String[] args) {
        String action = args[1].toLowerCase();
        List<String> lores = plugin.config.getStringList("exempt_item_lore");

        switch (action) {
            case "exempt":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getPrefix() + "Usage: /antiillegal lore exempt <lore>");
                } else {
                    String lore = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    if (!lores.contains(lore)) {
                        lores.add(lore);
                        plugin.config.set("exempt_item_lore", lores);
                        plugin.saveConfig();
                        sender.sendMessage(plugin.getPrefix() + "Lore exempted successfully.");
                    } else {
                        sender.sendMessage(plugin.getPrefix() + "Lore already exempted.");
                    }
                }
                break;

            case "unexempt":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getPrefix() + "Usage: /antiillegal lore unexempt <lore>");
                } else {
                    String lore = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    if (lores.contains(lore)) {
                        lores.remove(lore);
                        plugin.config.set("exempt_item_lore", lores);
                        plugin.saveConfig();
                        sender.sendMessage(plugin.getPrefix() + "Lore unexempted successfully.");
                    } else {
                        sender.sendMessage(plugin.getPrefix() + "Lore not found.");
                    }
                }
                break;

            case "list":
                sender.sendMessage(plugin.getPrefix() + "Exempt lores: " + lores);
                break;

            default:
                sender.sendMessage(plugin.getPrefix() + "Invalid action. Usage: /antiillegal lore <exempt|unexempt|list> [<lore>]");
                break;
        }
    }

    private void handleItemCommand(CommandSender sender, String[] args) {
        String action = args[1].toLowerCase();
        String itemName = args[2].toUpperCase();
        Material material = Material.matchMaterial(itemName);
        if (material == null) {
            sender.sendMessage(plugin.getPrefix() + "Invalid item name.");
            return;
        }

        List<String> items = plugin.config.getStringList("monitored_items");

        switch (action) {
            case "monitor":
                if (!items.contains(itemName)) {
                    items.add(itemName);
                    plugin.config.set("monitored_items", items);
                    plugin.saveConfig();
                    sender.sendMessage(plugin.getPrefix() + "Item added to the monitor list.");
                } else {
                    sender.sendMessage(plugin.getPrefix() + "Item is already being monitored.");
                }
                break;

            case "unmonitor":
                if (items.contains(itemName)) {
                    items.remove(itemName);
                    plugin.config.set("monitored_items", items);
                    plugin.saveConfig();
                    sender.sendMessage(plugin.getPrefix() + "Item removed from the monitor list.");
                } else {
                    sender.sendMessage(plugin.getPrefix() + "Item is not in the monitor list.");
                }
                break;

            default:
                sender.sendMessage(plugin.getPrefix() + "Invalid action. Usage: /antiillegal item <monitor|unmonitor> <item>");
                break;
        }
    }

    private void handleEnchantmentCommand(CommandSender sender, String[] args) {
        String action = args[1].toLowerCase();
        String enchantName = args[2].toLowerCase();
        Enchantment enchantment = Enchantment.getByName(enchantName.toUpperCase());
        if (enchantment == null) {
            sender.sendMessage(plugin.getPrefix() + "Invalid enchantment name.");
            return;
        }

        int maxLevel = enchantment.getMaxLevel();
        if (args.length > 3) {
            try {
                maxLevel = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getPrefix() + "Invalid max level. Please provide a number.");
                return;
            }
        }

        Map<String, Integer> enchantments = new HashMap<>();
        ConfigurationSection section = plugin.config.getConfigurationSection("monitored_enchantments");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                enchantments.put(key, section.getInt(key));
            }
        }

        switch (action) {
            case "monitor":
                if (!enchantments.containsKey(enchantName)) {
                    enchantments.put(enchantName, maxLevel);
                    plugin.config.set("monitored_enchantments", enchantments);
                    plugin.saveConfig();
                    sender.sendMessage(plugin.getPrefix() + "Enchantment added to the monitor list with max level " + maxLevel + ".");
                } else {
                    sender.sendMessage(plugin.getPrefix() + "Enchantment is already being monitored.");
                }
                break;

            case "unmonitor":
                if (enchantments.containsKey(enchantName)) {
                    enchantments.remove(enchantName);
                    plugin.config.set("monitored_enchantments", enchantments);
                    plugin.saveConfig();
                    sender.sendMessage(plugin.getPrefix() + "Enchantment removed from the monitor list.");
                } else {
                    sender.sendMessage(plugin.getPrefix() + "Enchantment is not in the monitor list.");
                }
                break;

            default:
                sender.sendMessage(plugin.getPrefix() + "Invalid action. Usage: /antiillegal enchantments <monitor|unmonitor> <enchantment> [maxlevel]");
                break;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> allOptions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("antiillegal.reload")) completions.add("reload");
            if (sender.hasPermission("antiillegal.lore")) completions.add("lore");
            if (sender.hasPermission("antiillegal.item")) completions.add("item");
            if (sender.hasPermission("antiillegal.enchantments")) completions.add("enchantments");

            allOptions = completions;
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "lore":
                    if (sender.hasPermission("antiillegal.lore")) {
                        completions.add("exempt");
                        completions.add("unexempt");
                        completions.add("list");
                    }
                    break;
                case "item":
                    if (sender.hasPermission("antiillegal.item")) {
                        completions.add("monitor");
                        completions.add("unmonitor");
                    }
                    break;
                case "enchantments":
                    if (sender.hasPermission("antiillegal.enchantments")) {
                        completions.add("monitor");
                        completions.add("unmonitor");
                    }
                    break;
            }
            allOptions = completions;
        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "lore":
                    if ("unexempt".equalsIgnoreCase(args[1]) && sender.hasPermission("antiillegal.lore")) {
                        allOptions = plugin.config.getStringList("exempt_item_lore");
                    }
                    break;
                case "item":
                    if (sender.hasPermission("antiillegal.item")) {
                        allOptions = Arrays.stream(Material.values())
                                .map(Material::name)
                                .collect(Collectors.toList());
                    }
                    break;
                case "enchantments":
                    if (sender.hasPermission("antiillegal.enchantments")) {
                        allOptions = Arrays.stream(Enchantment.values())
                                .map(Enchantment::getKey)
                                .map(key -> key.getKey())
                                .map(String::toLowerCase)
                                .collect(Collectors.toList());
                    }
                    break;
            }
        }

        if (!allOptions.isEmpty() && args.length > 0) {
            String lastWord = args[args.length - 1].toLowerCase();
            completions = allOptions.stream()
                    .filter(option -> option.toLowerCase().startsWith(lastWord))
                    .collect(Collectors.toList());
        }

        return completions;
    }
}