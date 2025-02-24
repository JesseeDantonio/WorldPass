package fr.jessee.worldPass;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Constructor;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.Bukkit.getConsoleSender;

public final class WorldPass extends JavaPlugin {
    private static Plugin instance;
    private static ConfigLoader configLoader;
    private Map<UUID, RestrictedWorld> restrictedWorlds;
    private final TimeValidator timeValidator = new TimeValidator();


    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        registerListeners((WorldPass) instance);

        configLoader = new ConfigLoader(this);
        loadConfigurations();

        startWorldAccessCheckTask();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadConfigurations() {
        restrictedWorlds = configLoader.loadWorldConfigs();
    }

    private void startWorldAccessCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, RestrictedWorld> entry : restrictedWorlds.entrySet()) {
                    if (!timeValidator.isWithinTimeRange(entry.getValue().startTime(), entry.getValue().endTime())) {
                        for (Player player : entry.getValue().world().getPlayers()) {
                            String startFormatted = String.format("%02d:%02d", entry.getValue().startTime().getHour(), entry.getValue().startTime().getMinute());
                            String endFormatted = String.format("%02d:%02d", entry.getValue().endTime().getHour(), entry.getValue().endTime().getMinute());

                            player.kickPlayer(ChatColor.RED + "Vous ne pouvez pas être dans le monde " + entry.getValue().world().getName() +
                                    " en dehors des heures autorisées (" + startFormatted + " - " + endFormatted + ").");
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L * 60); // Vérifie toutes les minutes
    }

    private void registerListeners(final WorldPass worldPass) {
        // Scanner le package pour trouver toutes les classes implémentant Listener
        Reflections reflections = new Reflections("fr.jessee.worldPass.listener", new SubTypesScanner(false));
        Set<Class<? extends Listener>> classes = reflections.getSubTypesOf(Listener.class);

        for (Class<? extends Listener> listenerClass : classes) {
            try {
                Listener listener;
                Constructor<? extends Listener> defaultConstructor;

                try {
                    defaultConstructor = listenerClass.getDeclaredConstructor(WorldPass.class);
                    listener = defaultConstructor.newInstance(this);
                } catch (NoSuchMethodException e) {
                    defaultConstructor = listenerClass.getDeclaredConstructor();
                    listener = defaultConstructor.newInstance();
                }

                Bukkit.getPluginManager().registerEvents(listener, this);

            } catch (Exception e) {
                getConsoleSender().sendMessage(e.getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
            }
        }
    }

    public static Plugin getInstance() {
        return instance;
    }

    public static ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public Map<UUID, RestrictedWorld> getRestrictedWorlds() {
        return restrictedWorlds;
    }

    public TimeValidator getTimeValidator() {
        return timeValidator;
    }
}
