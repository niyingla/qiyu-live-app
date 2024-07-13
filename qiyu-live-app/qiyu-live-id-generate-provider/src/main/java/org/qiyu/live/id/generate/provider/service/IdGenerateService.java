package org.qiyu.live.id.generate.provider.service;

import org.qiyu.live.id.generate.provider.dao.po.IdGeneratePO;

public interface IdGenerateService {
    /**
     * 获取有序id
     *
     * @param id
     * @return
     */
    Long getSeqId(Integer id);

    /**
     * 获取无序id
     *
     * @param id
     * @return
     */
    Long getUnSeqId(Integer id);

    void insertIdGeneratePO(IdGeneratePO idGeneratePO);
}
