package me.hsgamer.autoislandpurge;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
        var spawn = instance.getSettings().getSpawnLocation();
        if (spawn != null) {
            Bukkit.getScheduler().runTask(instance.getPlugin(), () -> player.teleport(spawn));
        }
    }
}
