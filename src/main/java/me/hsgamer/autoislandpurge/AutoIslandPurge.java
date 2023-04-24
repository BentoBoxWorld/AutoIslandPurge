package me.hsgamer.autoislandpurge;

import world.bentobox.bentobox.api.addons.Addon;

public class AutoIslandPurge extends Addon {
    private final Settings settings = new Settings(this);
    private final PurgeTask purgeTask = new PurgeTask(this);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings.setup();
        purgeTask.setup();

        registerListener(new TeleportWhenIslandDeletedListener(this));
    }

    @Override
    public void onDisable() {
        purgeTask.cancel();
    }

    @Override
    public void onReload() {
        purgeTask.cancel();
        reloadConfig();
        settings.setup();
        purgeTask.setup();
    }

    public Settings getSettings() {
        return settings;
    }
}
