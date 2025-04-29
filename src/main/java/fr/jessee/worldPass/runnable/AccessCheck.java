package fr.jessee.worldPass.runnable;

import fr.jessee.worldPass.RestrictedWorld;
import fr.jessee.worldPass.WorldPass;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
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
            // worldPass.getLogger().info("Checking.. " + entry.getKey() + " " +entry.getValue().world().getName());
            if (!(worldPass.getTimeValidator().isWithinTimeRange(entry.getValue().startTime(), entry.getValue().endTime()))) {
                for (Player player : entry.getValue().world().getPlayers().stream().filter(
                        player -> player.getWorld().getUID().equals(entry.getKey())
                ).toList()) {
                    if (entry.getValue().restrictOnlyNewPlayers()) {
                        // Récupère le temps de jeu du joueur
                        long playTimeTicks = 0;
                        try {
                            playTimeTicks = worldPass.getPlayTime().p(player.getUniqueId(), entry.getKey());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        long playTimeMinutes = playTimeTicks / (20 * 60);

                        //worldPass.getLogger().info("PlayTimeMinutes " + playTimeMinutes + " " + player.getName());
                        //worldPass.getLogger().info("PlayTimeMinutes " + playTimeMinutes + " < " + "minimumPlayTimeMinutes " + entry.getValue().minimumPlayTimeMinutes());
                        // Vérifie si le joueur a atteint le temps de jeu minimum
                        if (playTimeMinutes < entry.getValue().minimumPlayTimeMinutes()) {
                            player.kickPlayer(worldPass.getMessages().get("accessDeniedNewPlayer"));
                        }
                    } else {
                        Map<String, String> placeholders = getPlaceholders(entry);

                        player.kickPlayer(worldPass.getMessages().get("accessDenied", placeholders));
                    }
                }
            }
        }
    }

    private static Map<String, String> getPlaceholders(Map.Entry<UUID, RestrictedWorld> entry) {
        String startFormatted = String.format("%02d:%02d", entry.getValue().startTime().getHour(), entry.getValue().startTime().getMinute());
        String endFormatted = String.format("%02d:%02d", entry.getValue().endTime().getHour(), entry.getValue().endTime().getMinute());

        Map<String, String> placeholders = Map.of(
                "world", entry.getValue().world().getName(),
                "start", startFormatted,
                "end", endFormatted
        );
        return placeholders;
    }
}
