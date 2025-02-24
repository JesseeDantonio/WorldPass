package fr.jessee.worldPass.listener.player;

import fr.jessee.worldPass.RestrictedWorld;
import fr.jessee.worldPass.WorldPass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Optional;
import java.util.UUID;

public class Teleport implements Listener {
    private WorldPass worldPass;

    public Teleport(WorldPass worldPass) {
        this.worldPass = worldPass;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Optional<UUID> worldUUID = Optional.of(event.getTo().getWorld().getUID());

        if (worldPass.getRestrictedWorlds().containsKey(worldUUID.get())) {
            RestrictedWorld restrictedWorld = worldPass.getRestrictedWorlds().get(worldUUID.get());
            if (!(worldPass.getTimeValidator().isWithinTimeRange(restrictedWorld.startTime(), restrictedWorld.endTime()))) {
                event.setCancelled(true);
                player.sendMessage(
                        ChatColor.RED +
                                "Vous ne pouvez rejoindre " +
                                restrictedWorld.world().getName() +
                                " que pendant les heures autorisées !"
                );

            }
        }
    }
}
