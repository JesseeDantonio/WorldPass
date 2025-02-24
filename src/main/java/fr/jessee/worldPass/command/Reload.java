package fr.jessee.worldPass.command;

import fr.jessee.worldPass.WorldPass;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Reload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.hasPermission("timeaccess.reload")) { // Optionnel : gestion des permissions
            commandSender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'exécuter cette commande.");
        }

        commandSender.sendMessage(ChatColor.YELLOW + "Rechargement de la configuration...");
        WorldPass.getConfigLoader().loadWorldConfigs(); // Recharge les configurations à partir du fichier
        commandSender.sendMessage(ChatColor.GREEN + "Configuration rechargée avec succès !");
        return true;
    }
}
