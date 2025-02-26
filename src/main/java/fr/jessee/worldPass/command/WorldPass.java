package fr.jessee.worldPass.command;

import fr.jessee.worldPass.runnable.AccessCheck;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldPass implements CommandExecutor {
    private final fr.jessee.worldPass.WorldPass worldPass;

    public WorldPass(fr.jessee.worldPass.WorldPass worldPass) {
        this.worldPass = worldPass;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (command.getName().equalsIgnoreCase("worldpass")) {
            if (commandSender instanceof Player player) {
                player.sendMessage(
                        ChatColor.AQUA +
                                "Mise à jour des informations.."
                );
            } else {
                fr.jessee.worldPass.WorldPass.getInstance().getLogger().warning("Mise à jour des informations..");
            }
            if (worldPass.getAccessCheck() != null && worldPass.getAccessCheck().isCancelled()) {
                worldPass.getAccessCheck().cancel();
            }
            worldPass.updateConfig();
            worldPass.setAccessCheck(new AccessCheck(worldPass));
            worldPass.getAccessCheck().runTaskTimer(worldPass, 0L, 20L * 60);
        }
        return true;
    }
}
