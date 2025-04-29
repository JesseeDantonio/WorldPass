package fr.jessee.worldPass.listener.player;

import fr.jessee.worldPass.WorldPass;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.sql.SQLException;

public class WorldChange implements Listener {
    private final WorldPass worldPass;

    public WorldChange(WorldPass worldPass) {
        this.worldPass = worldPass;
    }
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        try {
            worldPass.getPlayTime().onWorldChange(event.getPlayer(), event.getFrom().getUID());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
