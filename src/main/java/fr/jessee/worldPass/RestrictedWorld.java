package fr.jessee.worldPass;

import org.bukkit.World;

import java.time.LocalTime;

public record RestrictedWorld(World world, LocalTime startTime, LocalTime endTime, boolean restrictOnlyNewPlayers, int minimumPlayTimeMinutes) {
}
