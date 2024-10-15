package live.amsleepy.antiillegalbukkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class AntiIllegal_Bukkit extends JavaPlugin implements Listener {

    public FileConfiguration config;
    private File logDirectory;
    private File logFile;
    private DiscordWebhook discordWebhook;

    private final String prefix = ChatColor.DARK_PURPLE + "[AntiIllegal] " + ChatColor.WHITE;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        logDirectory = new File(getDataFolder(), "logs");
        if (!logDirectory.exists()) {
            logDirectory.mkdirs();
        }
        createNewLogFile();

        AntiIllegalListener listener = new AntiIllegalListener(this);
        getServer().getPluginManager().registerEvents(listener, this);

        AntiIllegalCommandExecutor commandExecutor = new AntiIllegalCommandExecutor(this);
        getCommand("antiillegal").setExecutor(commandExecutor);
        getCommand("antiillegal").setTabCompleter(commandExecutor);

        String webhookUrl = config.getString("discord_webhook_url");
        if (webhookUrl != null && !webhookUrl.isEmpty()) {
            discordWebhook = new DiscordWebhook(webhookUrl);
        }

        List<String> monitoredItems = config.getStringList("monitored_items");
        getLogger().info(prefix + "Monitored Items: " + monitoredItems);

        ConfigurationSection enchantSection = config.getConfigurationSection("monitored_enchantments");
        if (enchantSection != null) {
            for (String key : enchantSection.getKeys(false)) {
                int maxLevel = enchantSection.getInt(key);
                getLogger().info(prefix + "Monitored Enchantment: " + key + " max level: " + maxLevel);
            }
        } else {
            getLogger().info(prefix + "No enchantments are being monitored.");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info(prefix + " AntiIllegal_Bukkit disabled!");
    }

    public synchronized void logItemRevert(String logEntry) {
        checkLogFileRotation();
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(logEntry + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String message = prefix + logEntry;
        Bukkit.getConsoleSender().sendMessage(message);

        if (discordWebhook != null) {
            try {
                String discordMessage = logEntry;
                String roleId = config.getString("discord_role_id");
                if (roleId != null && !roleId.isEmpty()) {
                    discordMessage = "<@&" + roleId + "> " + discordMessage;
                }
                if (discordMessage != null && !discordMessage.isEmpty()) {
                    discordWebhook.sendMessage(discordMessage);
                } else {
                    getLogger().warning("Discord message is null or empty. Skipping sending to Discord.");
                }
            } catch (IOException e) {
                getLogger().severe("Failed to send message to Discord: " + e.getMessage());
            }
        }
    }

    public String getPrefix() {
        return prefix;
    }

    private void checkLogFileRotation() {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (logFile == null || !logFile.getName().contains(currentDate)) {
            createNewLogFile();
        }
    }

    private void createNewLogFile() {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        logFile = new File(logDirectory, "AntiIllegal-" + currentDate + ".txt");
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}