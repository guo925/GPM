package com.gjx.gpms.archive;

/**
 * 冷热数据归档服务。
 */
public interface ArchiveService {

    /**
     * 处理archiveBatch相关逻辑。
     */
    void archiveBatch(Long batchId, Long operatorId);

    /**
     * 处理archiveHistoryBatches相关逻辑。
     */
    void archiveHistoryBatches(Long operatorId);
}
