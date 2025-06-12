CREATE TABLE `chat_record` (
                               `id` BIGINT(19) NOT NULL COMMENT '数据id',
                               `conversation_id` VARCHAR(100) NOT NULL COMMENT '对话id' COLLATE 'utf8mb4_bin',
                               `data` TEXT NULL DEFAULT NULL COMMENT '对话数据' COLLATE 'utf8mb4_bin',
                               `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `creater` BIGINT(19) NOT NULL COMMENT '创建人',
                               `updater` BIGINT(19) NOT NULL COMMENT '更新人',
                               PRIMARY KEY (`id`) USING BTREE,
                               INDEX `conversation_id_index` (`conversation_id`) USING BTREE,
                               INDEX `create_time_index` (`create_time`) USING BTREE
)
    COMMENT='对话记录'
COLLATE='utf8mb4_bin'
ENGINE=InnoDB
ROW_FORMAT=DYNAMIC
;
