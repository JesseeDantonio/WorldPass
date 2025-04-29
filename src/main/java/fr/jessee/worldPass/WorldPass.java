package fr.jessee.worldPass;

import fr.jessee.worldPass.feature.PlayTime;
import fr.jessee.worldPass.feature.YamlMessageProvider;
import fr.jessee.worldPass.iface.MessageProvider;
import fr.jessee.worldPass.runnable.AccessCheck;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.Bukkit.getConsoleSender;

public final class WorldPass extends JavaPlugin {
    private static Plugin instance;
    private ConfigLoader configLoader;
    private Map<UUID, RestrictedWorld> restrictedWorlds;
    private final TimeValidator timeValidator = new TimeValidator();
    private AccessCheck accessCheck;
    private LuckPerms luckPerms;
    private PlayTime playTime;
    private MessageProvider messages;

    @Override
    public void onLoad() {
        super.onLoad();
        if (getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            getLogger().severe("LuckPerms n'est pas installé ! Le plugin sera désactivé.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        registerListeners();
        registerCommands();
        updateConfig();
        messages = new YamlMessageProvider(this);

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            getLogger().info("LuckPerms API initialisée avec succès !");
        } else {
            getLogger().severe("Impossible d'obtenir l'API LuckPerms !");
            getServer().getPluginManager().disablePlugin(this);
        }

        try {
            playTime = new PlayTime(instance);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        accessCheck = new AccessCheck(this);
        accessCheck.runTaskTimer(this, 0L, 20L);
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
        this.reloadConfig();
        restrictedWorlds = configLoader.getWorldRestrictions();
    }

    private void registerListeners() {
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

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public Map<UUID, RestrictedWorld> getRestrictedWorlds() {
        return restrictedWorlds;
    }

    public TimeValidator getTimeValidator() {
        return timeValidator;
    }

    public AccessCheck getAccessCheck() {
        return accessCheck;
    }

    public void setAccessCheck(AccessCheck accessCheck) {
        this.accessCheck = accessCheck;
    }

    public LuckPerms getLuckPermsAPI() {
        return luckPerms;
    }

    public PlayTime getPlayTime() {
        return playTime;
    }

    public MessageProvider getMessages() {
        return messages;
    }
}
