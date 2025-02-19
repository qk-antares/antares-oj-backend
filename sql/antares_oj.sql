CREATE TABLE `user` (
  `uid` bigint unsigned NOT NULL COMMENT '用户id',
  `username` varchar(64) DEFAULT NULL COMMENT '用户名',
  `email` varchar(64) NOT NULL COMMENT '邮箱',
  `password` varchar(128) DEFAULT NULL COMMENT '密码',
  `user_role` varchar(64) NOT NULL DEFAULT 'user' COMMENT '用户权限',
  `access_key` char(40) DEFAULT NULL,
  `secret_key` char(40) DEFAULT NULL,
  `tags` varchar(512) DEFAULT '[]' COMMENT '用户标签',
  `signature` varchar(256) DEFAULT NULL COMMENT '个性签名',
  `sex` tinyint NOT NULL DEFAULT 1 COMMENT '性别',
  `avatar` varchar(256) DEFAULT NULL COMMENT '头像',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`uid`) using BTREE,
  UNIQUE KEY `email` (`email`) using BTREE,
  UNIQUE KEY `username` (`username`) using BTREE,
  KEY `idx_email_passowrd` (`email`,`password`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE `problem` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目id',
  `title` varchar(512) DEFAULT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `difficulty` varchar(64) DEFAULT '简单' COMMENT '难度',
  `tags` varchar(1024) DEFAULT '[]' COMMENT '标签列表',
  `answer` text COMMENT '题目答案',
  `submit_num` int NOT NULL DEFAULT '0' COMMENT '题目提交数',
  `accepted_num` int NOT NULL DEFAULT '0' COMMENT '题目通过数',
  `judge_case` text COMMENT '判题用例(json 数组)',
  `judge_config` text COMMENT '判题配置(json 对象)',
  `thumb_num` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `favour_num` int NOT NULL DEFAULT '0' COMMENT '收藏数',
  `user_id` bigint NOT NULL COMMENT '创建用户 id',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_difficulty` (`difficulty`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目';

CREATE TABLE `problem_submit` (
  `id` bigint unsigned NOT NULL COMMENT '题目提交id',
  `language` varchar(128) NOT NULL COMMENT '编程语言',
  `code` text NOT NULL COMMENT '用户代码',
  `judge_info` text COMMENT '判题信息(json 对象)',
  `status` int NOT NULL DEFAULT '0' COMMENT '判题状态(0 - 待判题、1 - 判题中、2 - 成功、3 - 失败)',
  `problem_id` bigint NOT NULL COMMENT '题目 id',
  `user_id` bigint NOT NULL COMMENT '创建用户 id',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_userId_status` (`user_id`,`status`),
  KEY `idx_userId_problemId` (`user_id`,`problem_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目提交';

# 以下为测试脚本

# 重置三个表
TRUNCATE TABLE `problem`;

ALTER TABLE `problem` AUTO_INCREMENT = 1;
