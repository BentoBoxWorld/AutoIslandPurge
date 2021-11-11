package me.hsgamer.autoislandpurge;

import world.bentobox.bentobox.api.addons.Addon;

import java.util.ArrayList;
import java.util.List;

public class AutoIslandPurge extends Addon {
    private final Settings settings = new Settings(this);
    private final List<PurgeTask> tasks = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings.setup();
        setupTasks();

        registerListener(new TeleportWhenIslandDeletedListener(this));
    }

    @Override
    public void onDisable() {
        clearTasks();
    }

    @Override
    public void onReload() {
        clearTasks();
        reloadConfig();
        settings.setup();
        setupTasks();
    }

    private void clearTasks() {
        tasks.forEach(PurgeTask::cancel);
        tasks.clear();
    }

    private void setupTasks() {
        settings.getEnabledGameModes().forEach(gameModeAddon -> {
            tasks.add(new PurgeTask(this, gameModeAddon));
            log("Auto-Purge will apply to " + gameModeAddon.getDescription().getName());
        });
    }

    public Settings getSettings() {
        return settings;
    }
}
