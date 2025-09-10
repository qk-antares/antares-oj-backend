pipeline {
    agent any

    // 使用Jenkins上的nodejs 18工具
    tools {
        maven 'maven 3.9.9'
    }

    stages {
        stage('Build') {
            steps {
                sh 'mvn clean compile -DskipTests'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Deploy') {
            steps {
                sshPublisher(publishers: [
                    sshPublisherDesc(
                        configName: 'host-deploy',
                        transfers: [
                            sshTransfer(
                                sourceFiles: 'antares-gateway/target/*.jar',
                                removePrefix: 'antares-gateway/target',
                                remoteDirectory: './backend/antares-oj-backend'
                            ),
                            sshTransfer(
                                sourceFiles: 'antares-judge/target/*.jar',
                                removePrefix: 'antares-judge/target',
                                remoteDirectory: './backend/antares-oj-backend'
                            ),
                            sshTransfer(
                                sourceFiles: 'antares-user/target/*.jar',
                                removePrefix: 'antares-user/target',
                                remoteDirectory: './backend/antares-oj-backend'
                            ),
                            sshTransfer(
                                sourceFiles: 'antares-code-sandbox/target/*.jar',
                                removePrefix: 'antares-code-sandbox/target',
                                remoteDirectory: './backend/antares-oj-backend'
                            ),
                            sshTransfer(
                                execCommand: '''
                                    echo "✅ 文件上传完成，开始执行部署任务"
                                    cd /software/app/backend/antares-oj-backend

                                    # 关闭旧进程并启动新进程
                                    for svc in antares-gateway antares-judge antares-user antares-code-sandbox; do
                                        JAR=$(ls ${svc}-*.jar 2>/dev/null | head -n 1)
                                        if [ -n "$JAR" ]; then
                                            PID=$(ps -ef | grep $JAR | grep -v grep | awk '{print $2}')
                                            if [ -n "$PID" ]; then
                                                kill -9 $PID
                                                echo "已杀死 $svc 旧进程: $PID"
                                            fi
                                            nohup /software/jdk-21.0.5/bin/java -jar $JAR --spring.config.location=/software/app/backend/config/$svc/application.yml > /software/app/backend/log/$svc.log 2>&1 &
                                            echo "$svc 已启动"
                                        else
                                            echo "$svc jar 未找到，跳过启动"
                                        fi
                                    done
                                    echo "✅ 所有服务已部署并尝试启动完毕"
                                '''
                            )
                        ],
                        usePromotionTimestamp: false,
                        verbose: true
                    )
                ])
            }
        }
    }

    post {
        success {
            echo '部署成功 ✅'
        }
        failure {
            echo '部署失败 ❌'
        }
    }
}