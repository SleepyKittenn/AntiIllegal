# AntiIllegal-Bukkit

![Java](https://img.shields.io/badge/Java-17-blue) 
![Version](https://img.shields.io/badge/version-1.1.0-yellow.svg)
![API-Version](https://img.shields.io/badge/api--version-1.13-lightgrey.svg)

AntiIllegal-Bukkit is a Minecraft plugin designed to monitor and manage illegal items and enchantments on your server. It helps maintain fair play by automatically reverting illegal items and notifying administrators.

## Features

- **Item Monitoring:** Detect and manage illegal items based on the monitored list.
- **Enchantment Monitoring:** Automatically detect and manage items with illegal enchantments.
- **Lore Exemption:** Configure item lores that are exempt from being marked as illegal.
- **Discord Integration:** Send logs and notifications to a specified Discord webhook.
- **Command Support:** Manage monitored items and enchantments dynamically with commands.
- **Log File Rotation:** Automatically rotate log files daily to manage file sizes and keep logs organized.

## Installation

1. **Download** the latest version of the plugin from the [releases](https://github.com/SleepyKittenn/AntiIllegal/releases) page.
2. **Place** the `AntiIllegal-Bukkit.jar` file into your server's `plugins` directory.
3. **Start** the server to generate the default configuration files.
4. **Edit** the `config.yml` file in the `plugins/AntiIllegal-Bukkit` directory to customize your settings.
5. **Reload** the server or use `/antiillegal reload` to apply the configuration changes.

## Configuration

The main configuration file `config.yml` allows you to customize the behavior of the AntiIllegal plugin:

```yaml
# Configuration file for AntiIllegal Bukkit plugin

# List of item lores that are exempt from being marked as illegal
exempt_item_lore:
  - "SpecialItemLore1"
  - "SpecialItemLore2"

# List of items to monitor
monitored_items:
  - DIAMOND_SWORD
  - GOLDEN_APPLE

# List of enchantments to monitor with their max levels
monitored_enchantments:
  sharpness: 5
  protection: 4
  efficiency: 5
  unbreaking: 3

# Discord webhook URL for sending notification logs
discord_webhook_url: "https://discord.com/api/webhooks/your-webhook-id/your-webhook-token"

# Optional: Discord role ID to ping in the messages
discord_role_id: "your-discord-role-id"
```

## Commands

- **/antiillegal reload**: Reload the AntiIllegal plugin configuration.
    - **Permission**: `antiillegal.reload`
- **/antiillegal lore <exempt|unexempt|list> <lore>**: Manage item lores for exemptions.
    - **Permission**: `antiillegal.lore`
- **/antiillegal item <monitor|unmonitor> <item>**: Manage items to monitor.
    - **Permission**: `antiillegal.item`
- **/antiillegal enchantments <monitor|unmonitor> <enchantment> [maxlevel]**: Manage monitored enchantments.
    - **Permission**: `antiillegal.enchantments`

## Permissions

- **antiillegal.reload**: Allows reloading the AntiIllegal plugin configuration.
    - **Default**: `op`
- **antiillegal.lore**: Allows managing lores for the AntiIllegal plugin.
    - **Default**: `op`
- **antiillegal.item**: Allows managing items for the AntiIllegal plugin.
    - **Default**: `op`
- **antiillegal.enchantments**: Allows managing enchantments for the AntiIllegal plugin.
    - **Default**: `op`

## Development

### Building From Source

1. **Clone** the repository:
    ```sh
    git clone https://github.com/your-username/AntiIllegal-Bukkit.git
    cd AntiIllegal-Bukkit
    ```

2. **Build** the plugin using Maven:
    ```sh
    mvn clean package
    ```

3. **Find** the compiled JAR in the `target` directory.

## Contributing

Contributions are welcome! Feel free to submit a pull request or open an issue to address bugs, suggest new features, or improve documentation.

## Credits

Developed by **sleepy**.

## Support

For any questions or issues, please open an issue on the [GitHub repository](https://github.com/SleepyKittenn/AntiIllegal/issues).

---

Thank you for using AntiIllegal-Bukkit! 🎉
