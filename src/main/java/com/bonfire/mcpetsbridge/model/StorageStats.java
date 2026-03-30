package com.bonfire.mcpetsbridge.model;

public record StorageStats(
        long snapshotCount,
        long rollbackJobCount,
        long auditLogCount,
        BackupRunSummary lastBackupRun
) {
}
