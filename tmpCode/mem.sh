#!/bin/bash

# 检查是否提供了时间间隔参数
if [ -z "$1" ]; then
  echo "Usage: $0 <interval_in_seconds>"
  exit 1
fi

# 时间间隔参数
INTERVAL="$1"
MEMORY_FILE="/sys/fs/cgroup/memory.current"

# 检查 memory.current 文件是否存在
if [ ! -f "$MEMORY_FILE" ]; then
  echo "Error: $MEMORY_FILE does not exist."
  exit 1
fi

# 使用子shell运行监控循环
(
  # 循环，按照指定间隔刷新内存占用
  while true; do
    cat "$MEMORY_FILE"
    sleep "$INTERVAL"
  done
) &

# 将子shell的进程ID保存，便于退出时终止
MONITOR_PID=$!

# 等待输入流中的字符
while true; do
  read -n 1 char  # 读取单个字符
  if [ "$char" = "q" ]; then
    kill "$MONITOR_PID"  # 终止监控进程
    wait "$MONITOR_PID"  # 等待进程结束
    exit 0
  fi
done
