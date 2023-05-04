package me.hsgamer.autoislandpurge;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class PurgeTask {
    private final AutoIslandPurge instance;
    private final Queue<Island> islands = new ConcurrentLinkedQueue<>();
    private BukkitTask checkTask;
    private BukkitTask deleteTask;

    public PurgeTask(AutoIslandPurge instance) {
        this.instance = instance;
    }

    private void checkIslands() {
        instance.getIslands().getIslands()
                .parallelStream()
                .filter(i -> instance.getSettings().isGameModeEnabled(i.getGameMode()))
                .filter(i -> !i.isSpawn())
                .filter(i -> !i.getPurgeProtected())
                .filter(i -> i.getWorld().getEnvironment().equals(World.Environment.NORMAL))
                .filter(Island::isOwned)
                .filter(i -> i.getMembers().size() <= instance.getSettings().getMaxMemberSize())
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

        UUID owner = island.getOwner();
        String log = String.format(
                "Island at %s (%s - %s) deleted",
                island.getSpawnPoint(World.Environment.NORMAL),
                owner,
                Optional.ofNullable(owner)
                        .map(Bukkit::getOfflinePlayer)
                        .map(OfflinePlayer::getName)
                        .orElse(null)
        );
        Bukkit.getScheduler().runTask(instance.getPlugin(), () -> {
            instance.getIslands().deleteIsland(island, true, null);
            instance.log(log);
        });
    }

    public void setup() {
        long checkTicks = instance.getSettings().getCheckTicks();
        long deleteTicks = instance.getSettings().getDeleteTicks();

        checkTask = Bukkit.getScheduler().runTaskTimerAsynchronously(instance.getPlugin(), this::checkIslands, checkTicks, checkTicks);
        deleteTask = Bukkit.getScheduler().runTaskTimerAsynchronously(instance.getPlugin(), this::deleteOneIsland, deleteTicks, deleteTicks);
    }

    public void cancel() {
        if (!checkTask.isCancelled()) checkTask.cancel();
        if (!deleteTask.isCancelled()) deleteTask.cancel();
        islands.clear();
    }
}
