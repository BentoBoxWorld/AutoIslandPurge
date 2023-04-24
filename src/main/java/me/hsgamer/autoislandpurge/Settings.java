package me.hsgamer.autoislandpurge;

import org.bukkit.Location;

import java.util.Collections;
import java.util.List;

public class Settings {
    private final AutoIslandPurge addon;
    private List<String> enabledGameModes = Collections.emptyList();
    private int offlineDays = 7;
    private int maxMemberSize = 1;
    private long checkTicks = 300;
    private long deleteTicks = 10;
    private Location spawnLocation;
    private boolean forcedSpawnLocation;

    public Settings(AutoIslandPurge addon) {
        this.addon = addon;
    }

    public void setup() {
        var config = addon.getConfig();

        enabledGameModes = config.getStringList("enabled-modes");
        offlineDays = config.getInt("offline-days-until-purge", offlineDays);
        maxMemberSize = config.getInt("purge-island-member-size", maxMemberSize);
        checkTicks = config.getLong("check-purge-ticks", checkTicks);
        deleteTicks = config.getLong("ticks-per-island-deleted", deleteTicks);
        try {
            var section = config.getConfigurationSection("spawn-location");
            if (section != null) {
                var map = section.getValues(false);
                spawnLocation = Location.deserialize(map);
            }
        } catch (Exception e) {
            // IGNORED
        }
        forcedSpawnLocation = config.getBoolean("forced-spawn-location", forcedSpawnLocation);
    }

    public List<String> getEnabledGameModes() {
        return enabledGameModes;
    }

    public int getOfflineDays() {
        return offlineDays;
    }

    public int getMaxMemberSize() {
        return maxMemberSize;
    }

    public long getCheckTicks() {
        return checkTicks;
    }

    public long getDeleteTicks() {
        return deleteTicks;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public boolean isForcedSpawnLocation() {
        return forcedSpawnLocation;
    }
}
