package fr.jessee.worldPass.runnable;

import fr.jessee.worldPass.RestrictedWorld;
import fr.jessee.worldPass.WorldPass;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class AccessCheck extends BukkitRunnable {
    private final WorldPass worldPass;

    public AccessCheck(WorldPass worldPass) {
        this.worldPass = worldPass;
    }

    @Override
    public void run() {
        for (Map.Entry<UUID, RestrictedWorld> entry : worldPass.getRestrictedWorlds().entrySet()) {
            //worldPass.getLogger().info("Checking.. " + entry.getKey() + " " +entry.getValue().world().getName());
            if (!(worldPass.getTimeValidator().isWithinTimeRange(entry.getValue().startTime(), entry.getValue().endTime()))) {
                for (Player player : entry.getValue().world().getPlayers()) {
                    if (entry.getValue().restrictOnlyNewPlayers()) {
                        // Récupère le temps de jeu du joueur
                        int playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
                        int playTimeMinutes = playTimeTicks / (20 * 60);

                        worldPass.getLogger().info("PlayTimeMinutes " + playTimeMinutes + " " + player.getName());
                        worldPass.getLogger().info("PlayTimeMinutes " + playTimeMinutes + " < " + "minimumPlayTimeMinutes " + entry.getValue().minimumPlayTimeMinutes());
                        //worldPass.getLogger().info("PlayTimeMinutes " + playTimeMinutes + " " + player.getName());
                        //worldPass.getLogger().info("PlayTimeMinutes " + playTimeMinutes + " < " + "minimumPlayTimeMinutes " + entry.getValue().minimumPlayTimeMinutes());
                        // Vérifie si le joueur a atteint le temps de jeu minimum
                        if (playTimeMinutes < entry.getValue().minimumPlayTimeMinutes()) {
                            player.kickPlayer("Vous ne pouvez pas entrer dans ce monde car vous êtes un joueur inexpérimenté !");
                        }
                    } else {
                        String startFormatted = String.format("%02d:%02d", entry.getValue().startTime().getHour(), entry.getValue().startTime().getMinute());
                        String endFormatted = String.format("%02d:%02d", entry.getValue().endTime().getHour(), entry.getValue().endTime().getMinute());

                        player.kickPlayer(ChatColor.RED + "Vous ne pouvez pas être dans le monde " + entry.getValue().world().getName() +
                                " en dehors des heures autorisées (" + startFormatted + " - " + endFormatted + ").");
                    }
                }
            }
        }
    }
}
