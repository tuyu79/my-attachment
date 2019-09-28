CREATE SCHEMA IF NOT EXISTS `attachment` DEFAULT CHARACTER SET utf8mb4;


CREATE TABLE IF NOT EXISTS `attachment`.`attachment_task`
(
    `id`                 BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增id',
    `task_no`            BIGINT(20) UNSIGNED NOT NULL COMMENT '任务编号',
    `vehicle_no`         BIGINT(20) UNSIGNED NOT NULL,
    `platform_alarm_uid` BIGINT(20) UNSIGNED NOT NULL COMMENT '平台报警唯一id',
    `file_count`         INT UNSIGNED        NOT NULL COMMENT '上传文件数量',
    `status`             INT UNSIGNED        NOT NULL COMMENT '上传状态(0-等待上传,1-正在上传,2-上传成功,3-上传失败,4-已取消)',
    `create_at`          DATETIME(3)         NOT NULL,
    `update_at`          DATETIME(3)         NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `task_no_UNIQUE` (`task_no` ASC),
    INDEX `idx_vehicle_no` (`vehicle_no` ASC)
)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COMMENT = '附件下载任务';


CREATE TABLE IF NOT EXISTS `attachment`.`attachment_file_task`
(
    `id`                 BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增id',
    `file_task_no` BIGINT(20) UNSIGNED NOT NULL COMMENT '附件任务编号',
    `attachment_task_no` BIGINT(20) UNSIGNED NOT NULL COMMENT '附件任务编号',
    `file_name`          VARCHAR(45)         NOT NULL COMMENT '文件名',
    `file_type`          INT UNSIGNED        NOT NULL COMMENT '文件类型',
    `file_size`          BIGINT(20) UNSIGNED NOT NULL COMMENT '文件大小',
    `is_completed`       INT UNSIGNED        NOT NULL COMMENT '是否完成',
    `create_at`          DATETIME(3)         NOT NULL COMMENT '创建时间',
    `update_at`          DATETIME(3)         NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_attachment_task_no` (`attachment_task_no` ASC)
)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COMMENT = '文件下载任务';
