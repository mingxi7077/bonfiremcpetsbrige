package com.bonfire.mcpetsbridge.command;

import com.bonfire.mcpetsbridge.BonfireMCpetsBridge;
import com.bonfire.mcpetsbridge.model.BackupRunSummary;
import com.bonfire.mcpetsbridge.model.CleanupSummary;
import com.bonfire.mcpetsbridge.model.PlayerReference;
import com.bonfire.mcpetsbridge.model.RollbackJobRecord;
import com.bonfire.mcpetsbridge.model.SnapshotRecord;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class BridgeCommand implements CommandExecutor, TabCompleter {

    private final BonfireMCpetsBridge plugin;

    public BridgeCommand(BonfireMCpetsBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(plugin.bridgeService().config().adminPermission())) {
            sender.sendMessage("[BMCB] No permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String root = args[0].toLowerCase(Locale.ROOT);
        return switch (root) {
            case "status" -> handleStatus(sender);
            case "reload" -> handleReload(sender);
            case "backup" -> handleBackup(sender, args);
            case "snapshot" -> handleSnapshot(sender, args);
            case "rollback" -> handleRollback(sender, args);
            case "cleanup" -> handleCleanup(sender, args);
            default -> {
                sendHelp(sender);
                yield true;
            }
        };
    }

    private boolean handleStatus(CommandSender sender) {
        sender.sendMessage("[BMCB] Status started asynchronously.");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> respond(sender, plugin.bridgeService().statusLines()));
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        try {
            plugin.reloadBridge();
            sender.sendMessage("[BMCB] Reloaded successfully.");
        } catch (Exception exception) {
            sender.sendMessage("[BMCB] Reload failed: " + exception.getMessage());
        }
        return true;
    }

    private boolean handleBackup(CommandSender sender, String[] args) {
        if (args.length < 2 || !"now".equalsIgnoreCase(args[1])) {
            sender.sendMessage("[BMCB] Usage: /bmcb backup now [player]");
            return true;
        }

        sender.sendMessage("[BMCB] Backup started asynchronously.");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String operator = sender.getName();
                BackupRunSummary summary;
                if (args.length >= 3) {
                    PlayerReference playerReference = plugin.bridgeService().resolvePlayerReference(args[2]);
                    if (playerReference == null) {
                        respond(sender, "[BMCB] Backup failed: player not found.");
                        return;
                    }
                    summary = plugin.bridgeService().runManualBackup(playerReference, operator);
                } else {
                    summary = plugin.bridgeService().runManualBackup(null, operator);
                }
                respond(sender, "[BMCB] Backup " + summary.status() + " run=" + summary.runId()
                        + " scanned=" + summary.scannedCount()
                        + " inserted=" + summary.insertedCount()
                        + " skipped=" + summary.skippedCount()
                        + " message=" + summary.message());
            } catch (Exception exception) {
                respond(sender, "[BMCB] Backup failed: " + exception.getMessage());
            }
        });
        return true;
    }

    private boolean handleSnapshot(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("[BMCB] Usage: /bmcb snapshot <list|info>");
            return true;
        }

        String sub = args[1].toLowerCase(Locale.ROOT);
        if ("list".equals(sub)) {
            if (args.length < 3) {
                sender.sendMessage("[BMCB] Usage: /bmcb snapshot list <player> [page] [pageSize]");
                return true;
            }
            sender.sendMessage("[BMCB] Snapshot list started asynchronously.");
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    PlayerReference playerReference = plugin.bridgeService().resolvePlayerReference(args[2]);
                    if (playerReference == null) {
                        respond(sender, "[BMCB] Player not found.");
                        return;
                    }
                    int page = args.length >= 4 ? Math.max(1, Integer.parseInt(args[3])) : 1;
                    int pageSize = args.length >= 5 ? Math.max(1, Math.min(50, Integer.parseInt(args[4]))) : 10;
                    long total = plugin.bridgeService().countSnapshots(playerReference);
                    int totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
                    List<SnapshotRecord> snapshots = plugin.bridgeService().listSnapshots(playerReference, page, pageSize);
                    List<String> lines = new ArrayList<>();
                    lines.add("[BMCB] Snapshots for " + playerReference.name() + " " + playerReference.uuid());
                    lines.add("[BMCB] page=" + page + "/" + totalPages + " pageSize=" + pageSize + " total=" + total);
                    if (snapshots.isEmpty()) {
                        lines.add("[BMCB] No snapshots found on this page.");
                    } else {
                        for (SnapshotRecord snapshot : snapshots) {
                            lines.add("[BMCB] id=" + snapshot.id() + " type=" + snapshot.snapshotType() + " at=" + snapshot.createdAt() + " hash=" + shortHash(snapshot.contentHash()));
                        }
                    }
                    respond(sender, lines);
                } catch (Exception exception) {
                    respond(sender, "[BMCB] Snapshot list failed: " + exception.getMessage());
                }
            });
            return true;
        }

        if ("info".equals(sub)) {
            if (args.length < 3) {
                sender.sendMessage("[BMCB] Usage: /bmcb snapshot info <snapshotId>");
                return true;
            }
            sender.sendMessage("[BMCB] Snapshot info started asynchronously.");
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    long snapshotId = Long.parseLong(args[2]);
                    Optional<SnapshotRecord> snapshotOptional = plugin.bridgeService().getSnapshot(snapshotId);
                    if (snapshotOptional.isEmpty()) {
                        respond(sender, "[BMCB] Snapshot not found.");
                        return;
                    }
                    SnapshotRecord snapshot = snapshotOptional.get();
                    List<String> lines = new ArrayList<>();
                    lines.add("[BMCB] Snapshot id=" + snapshot.id() + " player=" + snapshot.playerName() + " " + snapshot.playerUuid());
                    lines.add("[BMCB] type=" + snapshot.snapshotType() + " source=" + snapshot.sourceType() + " created=" + snapshot.createdAt());
                    lines.add("[BMCB] hash=" + snapshot.contentHash() + " namesLen=" + snapshot.namesRaw().length() + " invLen=" + snapshot.inventoriesRaw().length() + " dataLen=" + snapshot.dataRaw().length());
                    lines.add("[BMCB] note=" + snapshot.note());
                    respond(sender, lines);
                } catch (Exception exception) {
                    respond(sender, "[BMCB] Snapshot info failed: " + exception.getMessage());
                }
            });
            return true;
        }

        sender.sendMessage("[BMCB] Usage: /bmcb snapshot <list|info>");
        return true;
    }

    private boolean handleRollback(CommandSender sender, String[] args) {
        if (args.length >= 3 && "status".equalsIgnoreCase(args[1])) {
            sender.sendMessage("[BMCB] Rollback status started asynchronously.");
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    Optional<RollbackJobRecord> jobOptional = plugin.bridgeService().getRollbackJob(args[2]);
                    if (jobOptional.isEmpty()) {
                        respond(sender, "[BMCB] Rollback job not found.");
                        return;
                    }
                    RollbackJobRecord job = jobOptional.get();
                    List<String> lines = new ArrayList<>();
                    lines.add("[BMCB] job=" + job.jobUuid() + " player=" + job.playerName() + " " + job.playerUuid());
                    lines.add("[BMCB] status=" + job.status() + " targetSnapshot=" + job.targetSnapshotId() + " preSnapshot=" + job.preSnapshotId());
                    lines.add("[BMCB] message=" + job.message());
                    lines.add("[BMCB] created=" + job.createdAt() + " updated=" + job.updatedAt());
                    respond(sender, lines);
                } catch (Exception exception) {
                    respond(sender, "[BMCB] Rollback status failed: " + exception.getMessage());
                }
            });
            return true;
        }

        if (args.length >= 3 && "confirm".equalsIgnoreCase(args[1])) {
            sender.sendMessage("[BMCB] Rollback confirmation started asynchronously.");
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                String result = plugin.bridgeService().confirmRollback(args[2], sender.getName());
                respond(sender, "[BMCB] " + result);
            });
            return true;
        }

        if (args.length >= 4 && "preview".equalsIgnoreCase(args[1])) {
            return startRollbackPreview(sender, args[2], args[3]);
        }

        if (args.length < 3) {
            sender.sendMessage("[BMCB] Usage: /bmcb rollback <player> <snapshotId>");
            sender.sendMessage("[BMCB] Usage: /bmcb rollback preview <player> <snapshotId>");
            sender.sendMessage("[BMCB] Usage: /bmcb rollback confirm <token>");
            sender.sendMessage("[BMCB] Usage: /bmcb rollback status <jobUuid>");
            return true;
        }

        return startRollbackFlow(sender, args[1], args[2]);
    }

    private boolean startRollbackFlow(CommandSender sender, String playerInput, String snapshotInput) {
        if (plugin.bridgeService().config().rollback().requireConfirmation()) {
            return startRollbackPreview(sender, playerInput, snapshotInput);
        }

        sender.sendMessage("[BMCB] Rollback started asynchronously.");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PlayerReference playerReference = plugin.bridgeService().resolvePlayerReference(playerInput);
                if (playerReference == null) {
                    respond(sender, "[BMCB] Rollback failed: player not found.");
                    return;
                }
                long snapshotId = Long.parseLong(snapshotInput);
                String result = plugin.bridgeService().rollback(playerReference, snapshotId, sender.getName());
                respond(sender, "[BMCB] " + result);
            } catch (Exception exception) {
                respond(sender, "[BMCB] Rollback failed: " + exception.getMessage());
            }
        });
        return true;
    }

    private boolean startRollbackPreview(CommandSender sender, String playerInput, String snapshotInput) {
        sender.sendMessage("[BMCB] Rollback preview started asynchronously.");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PlayerReference playerReference = plugin.bridgeService().resolvePlayerReference(playerInput);
                if (playerReference == null) {
                    respond(sender, "[BMCB] Rollback preview failed: player not found.");
                    return;
                }
                long snapshotId = Long.parseLong(snapshotInput);
                String result = plugin.bridgeService().previewRollback(playerReference, snapshotId, sender.getName());
                respond(sender, "[BMCB] " + result);
            } catch (Exception exception) {
                respond(sender, "[BMCB] Rollback preview failed: " + exception.getMessage());
            }
        });
        return true;
    }

    private boolean handleCleanup(CommandSender sender, String[] args) {
        if (args.length < 2 || !"run".equalsIgnoreCase(args[1])) {
            sender.sendMessage("[BMCB] Usage: /bmcb cleanup run");
            return true;
        }

        sender.sendMessage("[BMCB] Cleanup started asynchronously.");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            CleanupSummary summary = plugin.bridgeService().runCleanup();
            respond(sender, "[BMCB] " + summary.message());
        });
        return true;
    }

    private void respond(CommandSender sender, String message) {
        plugin.getServer().getScheduler().runTask(plugin, () -> sender.sendMessage(message));
    }

    private void respond(CommandSender sender, List<String> messages) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (String message : messages) {
                sender.sendMessage(message);
            }
        });
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("[BMCB] /bmcb status");
        sender.sendMessage("[BMCB] /bmcb backup now [player]");
        sender.sendMessage("[BMCB] /bmcb snapshot list <player> [page] [pageSize]");
        sender.sendMessage("[BMCB] /bmcb snapshot info <snapshotId>");
        sender.sendMessage("[BMCB] /bmcb rollback <player> <snapshotId>");
        sender.sendMessage("[BMCB] /bmcb rollback preview <player> <snapshotId>");
        sender.sendMessage("[BMCB] /bmcb rollback confirm <token>");
        sender.sendMessage("[BMCB] /bmcb rollback status <jobUuid>");
        sender.sendMessage("[BMCB] /bmcb cleanup run");
        sender.sendMessage("[BMCB] /bmcb reload");
    }

    private String shortHash(String hash) {
        if (hash == null || hash.length() < 12) {
            return hash;
        }
        return hash.substring(0, 12);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("status");
            suggestions.add("backup");
            suggestions.add("snapshot");
            suggestions.add("rollback");
            suggestions.add("cleanup");
            suggestions.add("reload");
            return suggestions;
        }
        if (args.length == 2 && "backup".equalsIgnoreCase(args[0])) {
            suggestions.add("now");
            return suggestions;
        }
        if (args.length == 2 && "snapshot".equalsIgnoreCase(args[0])) {
            suggestions.add("list");
            suggestions.add("info");
            return suggestions;
        }
        if (args.length == 2 && "rollback".equalsIgnoreCase(args[0])) {
            suggestions.add("preview");
            suggestions.add("confirm");
            suggestions.add("status");
            return suggestions;
        }
        if (args.length == 2 && "cleanup".equalsIgnoreCase(args[0])) {
            suggestions.add("run");
            return suggestions;
        }
        return suggestions;
    }
}
