package com.bonfire.mcpetsbridge.model;

public record BackupRunSummary(
        long runId,
        int scannedCount,
        int insertedCount,
        int skippedCount,
        String status,
        String message
) {
}

