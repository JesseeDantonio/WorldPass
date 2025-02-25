package fr.jessee.worldPass;

import fr.jessee.worldPass.runnable.AccessCheck;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.Bukkit.getConsoleSender;

public final class WorldPass extends JavaPlugin {
    private static Plugin instance;
    private static ConfigLoader configLoader;
    private Map<UUID, RestrictedWorld> restrictedWorlds;
    private final TimeValidator timeValidator = new TimeValidator();
    private AccessCheck accessCheck;


    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        registerListeners();
        registerCommands();
        updateConfig();

        accessCheck = new AccessCheck(this);
        accessCheck.runTaskTimer(this, 0L, 20L * 60);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (accessCheck != null && !(accessCheck.isCancelled())) {
            accessCheck.cancel();
        }
    }

    public void updateConfig() {
        if (configLoader == null) {
            configLoader = new ConfigLoader(this);
        }
        restrictedWorlds = configLoader.getWorldRestrictions();
        isRestrictOnlyNewPlayersEnabled = configLoader.isRestrictOnlyNewPlayersEnabled();
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

    private void registerCommands() {
        Reflections reflections = new Reflections("fr.jessee.worldPass.command", new SubTypesScanner(false));
        Set<Class<? extends CommandExecutor>> classes = reflections.getSubTypesOf(CommandExecutor.class);

        for (Class<? extends CommandExecutor> commandClass : classes) {
            try {
                Constructor<? extends CommandExecutor> defaultConstructor;
                CommandExecutor commandExecutor;

                try {
                    defaultConstructor = commandClass.getDeclaredConstructor(WorldPass.class);
                    commandExecutor = defaultConstructor.newInstance(this);
                } catch (NoSuchMethodException e) {
                    defaultConstructor = commandClass.getDeclaredConstructor();
                    commandExecutor = defaultConstructor.newInstance();
                }

                String commandName = commandClass.getSimpleName().toLowerCase();

                CommandExecutor finalCommandExecutor = commandExecutor;
                getCommandOptional(commandName).ifPresent(cmd -> cmd.setExecutor(finalCommandExecutor));
            } catch (Exception e) {
                getConsoleSender().sendMessage(e.getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
            }
        }
    }

    private Optional<PluginCommand> getCommandOptional(String name) {
        return Optional.ofNullable(this.getCommand(name));
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
