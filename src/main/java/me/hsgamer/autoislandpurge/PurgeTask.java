package me.hsgamer.autoislandpurge;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class PurgeTask {
    private final AutoIslandPurge instance;
    private final GameModeAddon gameModeAddon;
    private final Queue<Island> islands = new ConcurrentLinkedQueue<>();
    private final BukkitTask checkTask;
    private final BukkitTask deleteTask;

    public PurgeTask(AutoIslandPurge instance, GameModeAddon gameModeAddon) {
        this.instance = instance;
        this.gameModeAddon = gameModeAddon;

        long checkTicks = instance.getSettings().getCheckTicks();
        long deleteTicks = instance.getSettings().getDeleteTicks();

        checkTask = Bukkit.getScheduler().runTaskTimerAsynchronously(instance.getPlugin(), this::checkIslands, checkTicks, checkTicks);
        deleteTask = Bukkit.getScheduler().runTaskTimerAsynchronously(instance.getPlugin(), this::deleteOneIsland, deleteTicks, deleteTicks);
    }

    private void checkIslands() {
        instance.getIslands().getIslands(gameModeAddon.getOverWorld())
                .parallelStream()
                .filter(i -> !i.isSpawn())
                .filter(i -> !i.getPurgeProtected())
                .filter(Island::isOwned)
                .filter(i -> i.getMembers().size() == 1)
                .filter(i -> {
                    var uuid = i.getOwner();
                    if (uuid == null) {
                        return false;
                    }
                    var player = Bukkit.getOfflinePlayer(uuid);
                    if (player.isOnline() || !player.hasPlayedBefore()) {
                        return false;
                    }
                    long currentTime = System.currentTimeMillis();
                    long lastPlayed = player.getLastPlayed();
                    long duration = currentTime - lastPlayed;
                    long days = TimeUnit.MILLISECONDS.toDays(duration);
                    return days > instance.getSettings().getOfflineDays();
                })
                .filter(i -> !islands.contains(i))
                .forEach(islands::add);
    }

    private void deleteOneIsland() {
        Optional.ofNullable(islands.poll()).ifPresent(this::scheduleDelete);
    }

    private void scheduleDelete(Island island) {
        if (island.isDeleted()) {
            return;
        }
        if (
                Optional.ofNullable(island.getOwner())
                        .map(Bukkit::getOfflinePlayer)
                        .map(OfflinePlayer::isOnline)
                        .orElse(false)
        ) {
            return;
        }
        String log = String.format(
                "Island at %s (%s - %s) deleted",
                island.getSpawnPoint(World.Environment.NORMAL),
                island.getOwner(),
                Optional.ofNullable(island.getOwner())
                        .map(Bukkit::getOfflinePlayer)
                        .map(OfflinePlayer::getName)
                        .orElse(null)
        );
        Bukkit.getScheduler().runTask(instance.getPlugin(), () -> {
            gameModeAddon.getIslands().deleteIsland(island, true, null);
            instance.log(log);
        });
    }

    public void cancel() {
        if (!checkTask.isCancelled()) checkTask.cancel();
        if (!deleteTask.isCancelled()) deleteTask.cancel();
    }
}
