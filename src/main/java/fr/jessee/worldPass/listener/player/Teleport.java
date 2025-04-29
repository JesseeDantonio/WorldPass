package fr.jessee.worldPass.listener.player;

import fr.jessee.worldPass.RestrictedWorld;
import fr.jessee.worldPass.WorldPass;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Teleport implements Listener {
    private final WorldPass worldPass;

    public Teleport(WorldPass worldPass) {
        this.worldPass = worldPass;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) throws SQLException {
        Player player = event.getPlayer();
        Optional<UUID> worldUUID = Optional.of(event.getTo().getWorld().getUID());

        if (worldPass.getRestrictedWorlds().containsKey(worldUUID.get())) {
            RestrictedWorld restrictedWorld = worldPass.getRestrictedWorlds().get(worldUUID.get());
            if (!(worldPass.getTimeValidator().isWithinTimeRange(restrictedWorld.startTime(), restrictedWorld.endTime()))) {
                if (restrictedWorld.restrictOnlyNewPlayers()) {
                    long playTimeTicks = worldPass.getPlayTime().p(player.getUniqueId(), worldUUID.get());
                    long playTimeMinutes = playTimeTicks / (20 * 60);
                    if (playTimeMinutes < restrictedWorld.minimumPlayTimeMinutes()) {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }

                if (event.isCancelled()) {
                    player.sendMessage(worldPass.getMessages().get("teleportCancelled", Map.of("world", restrictedWorld.world().getName())));
                }
            }
        }
    }
}
