package com.gjx.gpms.archive;

/**
 * 冷热数据归档服务。
 */
public interface ArchiveService {

    void archiveBatch(Long batchId, Long operatorId);

    void archiveHistoryBatches(Long operatorId);
}
