package fr.jessee.worldPass;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalTime;
import java.util.*;

public class ConfigLoader {
    private final JavaPlugin plugin;

    public ConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Map<UUID, RestrictedWorld> loadWorldConfigs() {
        Map<UUID, RestrictedWorld> worldConfigs = new HashMap<>();


        ConfigurationSection worldsSection = plugin.getConfig().getConfigurationSection("worlds");
        if (worldsSection == null) {
            plugin.getLogger().warning("Aucune configuration de mondes trouvée dans config.yml !");
            return worldConfigs;
        }

        for (String worldName : worldsSection.getKeys(false)) {
            Optional<World> optionalWorld = Optional.ofNullable(Bukkit.getWorld(worldName));
            int startHour = worldsSection.getInt(worldName + ".startHour");
            int startMinute = worldsSection.getInt(worldName + ".startMinute");
            int endHour = worldsSection.getInt(worldName + ".endHour");
            int endMinute = worldsSection.getInt(worldName + ".endMinute");
            boolean restrictOnlyNewPlayers = worldsSection.getBoolean(worldName + ".restrictOnlyNewPlayers");
            int minimumPlayTimeMinutes = worldsSection.getInt(worldName + ".minimumPlayTimeMinutes");

            LocalTime startTime = LocalTime.of(startHour, startMinute);
            LocalTime endTime = LocalTime.of(endHour, endMinute);

            optionalWorld.ifPresent(world -> {
                worldConfigs.put(world.getUID(), new RestrictedWorld(world, startTime, endTime, restrictOnlyNewPlayers ,minimumPlayTimeMinutes));
            });
        }

        return worldConfigs;
    }
}
