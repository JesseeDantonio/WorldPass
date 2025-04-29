package fr.jessee.worldPass.feature;

import fr.jessee.worldPass.iface.MessageProvider;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Map;

public class YamlMessageProvider implements MessageProvider {

    private final FileConfiguration config;

    public YamlMessageProvider(Plugin plugin) {
        File file = new File(plugin.getDataFolder(), "message.yml");
        if (!file.exists()) {
            plugin.saveResource("message.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public String get(String key) {
        String raw = config.getString(key, "Message non trouvé pour: " + key);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    @Override
    public String get(String key, Map<String, String> placeholders) {
        String message = get(key); // déjà coloré
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return message;
    }
}


