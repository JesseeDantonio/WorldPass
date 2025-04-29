package fr.jessee.worldPass.listener.player;

import fr.jessee.worldPass.WorldPass;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class Quit implements Listener {
    private final WorldPass worldPass;

    public Quit(WorldPass worldPass) {
        this.worldPass = worldPass;
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        try {
            worldPass.getPlayTime().onQuit(event.getPlayer());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
