package fr.jessee.worldPass.command;

import fr.jessee.worldPass.runnable.AccessCheck;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getConsoleSender;

public class WorldPass implements CommandExecutor {
    private final fr.jessee.worldPass.WorldPass worldPass;

    public WorldPass(fr.jessee.worldPass.WorldPass worldPass) {
        this.worldPass = worldPass;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (command.getName().equalsIgnoreCase("worldpass") && args.length > 0) {
            return switch (args[0].toLowerCase()) {
                case "update", "reload", "restart" -> this.handleReload(commandSender);
                default -> {
                    String message = "Commande inconnue.";
                    if (commandSender instanceof Player player) {
                        player.sendMessage(ChatColor.RED + message);
                    } else {
                        getConsoleSender().sendMessage(message);
                    }
                    yield true;
                }
            };
        }
        return true;
    }

    private boolean handleReload(CommandSender commandSender) {
        if (commandSender instanceof Player player) {
            if (!(player.hasPermission("worldpass.reload"))) {
                player.sendMessage(
                        ChatColor.RED +
                                "Vous n'avez pas la permission !"
                );
            }
            player.sendMessage(
                    ChatColor.YELLOW +
                            "Mise à jour des informations en cours."
            );
        } else {
            fr.jessee.worldPass.WorldPass.getInstance().getLogger().warning("Mise à jour des informations en cours.");
        }
        if (worldPass.getAccessCheck() != null && worldPass.getAccessCheck().isCancelled()) {
            worldPass.getAccessCheck().cancel();
        }
        worldPass.updateConfig();
        worldPass.setAccessCheck(new AccessCheck(worldPass));
        worldPass.getAccessCheck().runTaskTimer(worldPass, 0L, 20L * 60);
        if (commandSender instanceof Player player) {
            player.sendMessage(
                    ChatColor.GREEN +
                            "Mise à jour des informations terminée."
            );
        } else {
            fr.jessee.worldPass.WorldPass.getInstance().getLogger().warning("Mise à jour des informations terminée.");
        }
        return true;
    }
}
