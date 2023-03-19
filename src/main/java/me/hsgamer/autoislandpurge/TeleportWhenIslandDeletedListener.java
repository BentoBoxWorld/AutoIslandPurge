package me.hsgamer.autoislandpurge;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

public record TeleportWhenIslandDeletedListener(AutoIslandPurge instance) implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var location = player.getLocation();
        if (!instance.getPlugin().getIWM().inWorld(location)) {
            return;
        }
        var island = instance.getIslands().getIslandAt(player.getLocation());
        if (island.isPresent() && !island.get().isDeleted()) {
            return;
        }

        Location spawnLocation = Optional.ofNullable(location.getWorld())
                .map(w -> instance.getIslands().getSpawnPoint(w))
                .filter(l -> !instance.getSettings().isForcedSpawnLocation())
                .orElseGet(() -> instance.getSettings().getSpawnLocation());

        if (spawnLocation != null) {
            Bukkit.getScheduler().runTask(instance.getPlugin(), () -> player.teleport(spawnLocation));
        }
    }
}
