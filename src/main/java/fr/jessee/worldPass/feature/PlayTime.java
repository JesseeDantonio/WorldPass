package fr.jessee.worldPass.feature;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayTime {
    private final Connection connection;
    private final Map<UUID, Long> joinTimestamps = new HashMap<>();

    public PlayTime(Plugin plugin) throws SQLException {
        File dbFile = new File(plugin.getDataFolder(), "playtime.db");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder();
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS play_time (uuid TEXT, uuid_world TEXT, time_played_ms INTEGER, PRIMARY KEY (uuid, uuid_world))");
        }
    }

    public void onWorldChange(Player player, UUID fromWorld) throws SQLException {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (joinTimestamps.containsKey(uuid)) {
            long sessionTime = now - joinTimestamps.get(uuid);
            updatePlayTime(uuid, fromWorld, sessionTime);
        }

        joinTimestamps.put(uuid, now);
    }

    public void onQuit(Player player) throws SQLException {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        UUID uuidWorld = player.getWorld().getUID();

        if (joinTimestamps.containsKey(uuid)) {
            long sessionTime = now - joinTimestamps.get(uuid);
            updatePlayTime(uuid, uuidWorld, sessionTime);
        }

        joinTimestamps.remove(uuid);
    }

    private void updatePlayTime(UUID uuid, UUID uuidWorld, long addedTime) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO play_time (uuid, uuid_world, time_played_ms) VALUES (?, ?, ?) " +
                        "ON CONFLICT(uuid, uuid_world) DO UPDATE SET time_played_ms = time_played_ms + ?"
        )) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, uuidWorld.toString());
            stmt.setLong(3, addedTime);
            stmt.setLong(4, addedTime);
            stmt.executeUpdate();
        }
    }

    public long p(UUID uuid, UUID uuidWorld) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT time_played_ms FROM play_time WHERE uuid = ? AND uuid_world = ?"
        )) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, uuidWorld.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getLong("time_played_ms") : 0;
        }
    }

    public void close() throws SQLException {
        connection.close();
    }
}

