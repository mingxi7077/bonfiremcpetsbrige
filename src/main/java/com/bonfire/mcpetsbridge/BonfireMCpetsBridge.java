package com.bonfire.mcpetsbridge;

import com.bonfire.mcpetsbridge.command.BridgeCommand;
import com.bonfire.mcpetsbridge.config.BridgeConfig;
import com.bonfire.mcpetsbridge.service.BridgeService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class BonfireMCpetsBridge extends JavaPlugin {

    private BridgeService bridgeService;
    private BukkitTask backupTask;
    private BukkitTask cleanupTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        bridgeService = new BridgeService(this);

        try {
            reloadBridge();
        } catch (Exception exception) {
            getLogger().severe("Failed to enable BonfireMCpetsBridge: " + exception.getMessage());
            exception.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PluginCommand command = getCommand("bonfiremcpetsbridge");
        if (command != null) {
            BridgeCommand executor = new BridgeCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        getLogger().info("BonfireMCpetsBridge enabled.");
    }

    @Override
    public void onDisable() {
        cancelTasks();
        if (bridgeService != null) {
            bridgeService.close();
        }
    }

    public void reloadBridge() throws Exception {
        cancelTasks();
        reloadConfig();
        BridgeConfig config = BridgeConfig.from(getConfig());
        bridgeService.reload(config);

        if (config.enabled()) {
            long backupTicks = Math.max(20L, config.backup().intervalMinutes() * 60L * 20L);
            long cleanupTicks = 24L * 60L * 60L * 20L;
            backupTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> bridgeService.runScheduledBackup(), backupTicks, backupTicks);
            cleanupTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> bridgeService.runCleanup(), cleanupTicks, cleanupTicks);
            getLogger().info("BonfireMCpetsBridge scheduled backup every " + config.backup().intervalMinutes() + " minute(s).");
        } else {
            getLogger().info("BonfireMCpetsBridge is disabled in config.yml.");
        }
    }

    public BridgeService bridgeService() {
        return bridgeService;
    }

    private void cancelTasks() {
        if (backupTask != null) {
            backupTask.cancel();
            backupTask = null;
        }
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
    }
}

