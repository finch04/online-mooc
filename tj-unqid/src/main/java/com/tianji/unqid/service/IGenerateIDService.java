package com.tianji.unqid.service;

public interface IGenerateIDService {
    /**
     * 获取有序id
     *
     * @return
     */
    Long getSeqId();

    /**
     * 获取无序id
     *
     * @return
     */
    Long getUnSeqId();
}
