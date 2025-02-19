create database cse_kg;

create table if not exists  `crawl_task` (
    `id` bigint not null auto_increment comment '爬取任务id',
    `root_url` varchar(512) not null comment '根词条url',
    `root_name` varchar(128) not null comment '根词条名称',
    `type` varchar(32) not null default 'baike' comment '爬取类型（baike，wikipedia）',
    `score_threshold` int not null default 6 comment '相关性阈值',
    `max_depth` int default 1 comment '爬取的最大深度',
    `status` tinyint not null default 0 comment '任务状态，0是等待执行，1是正在执行，2是执行完成，3是执行失败',
    `log` text default null comment '任务日志，用于记录任务执行失败时的信息',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬取任务';

create table if not exists  `lemma` (
    `id` bigint not null auto_increment comment '词条id',
    `task_id` bigint not null comment '任务id',
    `depth` int not null comment '词条所处任务的深度',
    `url` varchar(512) not null comment '词条url',
    `name` varchar(128) not null comment '词条名称',
    `title` varchar(128) default null comment '词条标题',
    `score` int default 0 comment '词条和主题的相关性分数',
    `content` varchar(256) default null comment '词条内容txt保存路径',
    `status` tinyint not null default 0 comment '词条状态，0是等待爬取，1是爬取成功，2是爬取失败，3是爬取退出（相关性未达到阈值）',
    `log` text default null comment '爬取日志，用于记录爬取失败时的信息',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_taskId_status_depth_createTime` (`task_id`, `status`, `depth`, `create_time`), # 查询下一个爬取词条
    KEY `idx_url` (`url`) # 查询词条是否已经爬取过了
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='百度百科词条';

create table if not exists  `lemma_link` (
    `id` bigint not null auto_increment comment '关系id',
    `from_lemma_id` bigint not null comment '父词条id',
    `to_lemma_id` bigint not null comment '被引用词条id',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='词条关系表';

create table if not exists  `chat` (
    `id` bigint not null auto_increment comment '对话id',
    `msg` text not null comment '用户输入的提问',
    `type` varchar(32) not null default 'local' comment '提问类型（local，global）',
    `status` tinyint not null default 0 comment '对话状态，0是已发送，1是正在处理，2是处理成功，3是处理失败',
    `res` text default null comment 'GraphRAG的回复',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天对话表';

create table if not exists `entity` (
    `id` bigint not null comment '实体id，对应human_readable_id',
    `title` varchar(512) comment '实体标题',
    `type` varchar(128) comment '实体类型',
    `description` text comment '实体描述',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实体表';

CREATE DATABASE IF NOT EXISTS SoftwareRelations;

USE SoftwareRelations;

create table if not exists `relationship` (
    `id` bigint not null comment '关系id，对应human_readable_id',
    `source` varchar(512) comment '源实体标题',
    `target` varchar(512) comment '目标实体标题',
    `description` text comment '关系描述',
    `weight` float comment '关系的重要性权重',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关系表';



# 以下是开发中测试的SQL语句，不用执行

# 重置三个表
TRUNCATE TABLE `crawl_task`;
TRUNCATE TABLE `lemma`;
TRUNCATE TABLE `lemma_link`;

ALTER TABLE `crawl_task` AUTO_INCREMENT = 1;
ALTER TABLE `lemma` AUTO_INCREMENT = 1;
ALTER TABLE `lemma_link` AUTO_INCREMENT = 1;

# 下一个待爬取词条
SELECT *
FROM `lemma`
WHERE `task_id` = :taskId
  AND `status` = 0
  AND `depth` <= (
      select `max_depth`
      from `crawl_task`
      where `id` = :taskId
    )
ORDER BY `depth`, `create_time`
LIMIT 1;

# 将爬取出错的词条重新设置为待爬
update `lemma` set `status`=0 where `status`=2 and `task_id` = :taskId;

# 将爬取成功的log设置为null（这些词条一般是第一次爬取失败了，比如说被百度封IP了，但重置状态后爬取成功了）
update `lemma` set `log`=null where `status`=3;
update `lemma` set `log`=null where `status`=1 and `content` is not null;

# 删除无效的引用
select * from `lemma_link`
         where `from_lemma_id` not in (select `id` from `lemma`)
            or `to_lemma_id` not in (select `id` from `lemma`);

select * from lemma where score > 10;