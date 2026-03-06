## 后端模块

# 数据质量监控平台 - 后端服务

## 简介
后端服务是数据质量监控平台的核心控制层，基于 Spring Boot 3 构建。它提供 RESTful API 用于规则管理、离线任务调度、告警记录查询，同时作为实时指标的查询入口（从 Redis 读取）和实时告警的查询入口（从 HBase 扫描）。系统通过 Quartz 调度离线 Spark 作业，并通过回调接口接收作业执行状态。

## 技术栈
- **Java 17**
- **Spring Boot 3.2.4**
- **Spring Data JPA**（MySQL 8.0）
- **Quartz 2.3.2**（任务调度）
- **Redis 5.0.7**（实时指标缓存）
- **HBase 2.4.11**（实时告警存储）
- **Maven**（项目构建）

## 集群环境
本系统部署在由三台 Ubuntu 虚拟机组成的集群上：
- **node1**：运行后端服务、MySQL、Redis
- **node2**：运行 HBase Master、Kafka Broker
- **node3**：运行 HBase RegionServer、Spark 作业提交节点

实际 IP 和主机名请根据你的环境替换。

## 配置说明
配置文件位于 `src/main/resources/application.yml`。主要配置项如下（敏感信息已用占位符替换）：

```yaml
server:
  port: 2006

spring:
  datasource:
    url: jdbc:mysql://node1:3306/quality?useSSL=false&serverTimezone=Asia/Shanghai
    username: your_mysql_username
    password: your_mysql_password
  jpa:
    hibernate:
      ddl-auto: update   # 开发环境可使用 update，生产建议 validate
    show-sql: false
  redis:
    host: node1
    port: 6379
    password: your_redis_password   # 若无密码则留空

# HBase 配置
hbase:
  zookeeper:
    quorum: node2,node3
    port: 2181

# Kafka 配置（用于实时规则管理）
kafka:
  bootstrap-servers: node2:9092

# 钉钉告警机器人 token
dingtalk:
  webhook: https://oapi.dingtalk.com/robot/send?access_token=your_dingtalk_token

# 自定义回调地址（Spark 作业完成后回调）
callback:
  spark-job: http://node1:2006/api/callback/spark-job
```

**注意**：请务必将 `your_mysql_username`、`your_mysql_password`、`your_redis_password`、`your_dingtalk_token` 等替换为真实值，并确保这些敏感信息**不要提交到代码仓库**（建议使用环境变量或配置中心管理）。

## 数据库初始化
1. 在 MySQL 中创建数据库：
   ```sql
   CREATE DATABASE quality CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. 首次启动时，若 `spring.jpa.hibernate.ddl-auto` 设置为 `update`，Hibernate 会自动创建表。生产环境建议手动执行建表脚本（位于 `src/main/resources/db/migration` 目录下）。

## 编译打包
```bash
mvn clean package
```
打包成功后，在 `target/` 目录下生成 `quality-platform.jar`。

## 启动服务
```bash
java -jar target/quality-platform.jar
```
默认端口为 2006，可通过命令行参数修改，例如：
```bash
java -jar target/quality-platform.jar --server.port=8080
```

## API 文档
启动服务后，访问 `http://node1:2006/swagger-ui.html` 可查看 Swagger 文档（如果项目已集成）。核心接口概览：

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/rules` | 获取所有规则 |
| POST | `/api/rules` | 创建规则 |
| PUT | `/api/rules/{id}` | 更新规则 |
| DELETE | `/api/rules/{id}` | 删除规则 |
| POST | `/api/rules/trigger/{id}` | 手动触发规则（立即执行一次） |
| GET | `/api/tasks` | 获取所有任务实例 |
| GET | `/api/alerts` | 获取离线告警 |
| PUT | `/api/alerts/{id}/confirm` | 确认告警（需提供确认人） |
| GET | `/api/realtime-alerts` | 获取实时告警（支持按规则、时间过滤） |
| GET | `/api/stats/trend` | 最近任务趋势（需提供 start/end 日期） |
| GET | `/api/stats/pass-rates` | 各规则通过率 |
| GET | `/api/stats/realtime/latest` | 所有实时规则的最新指标 |

详细请求/响应格式可参考代码中的 DTO 类或 Swagger 文档。

## 与其它模块的交互
- **前端**：前端通过 `/api` 代理将请求转发至后端（默认 `http://node1:2006/api`）。
- **实时 Spark Streaming 作业**：作业启动时会调用 `/api/rules/realtime` 获取所有启用的实时规则；计算出的窗口指标写入 Redis，触发的告警写入 HBase。
- **离线 Spark 作业**：后端通过 Quartz 调度或手动触发时，使用 `spark-submit` 提交 `spark-quality-job`，作业完成后回调 `/api/callback/spark-job` 更新任务状态。

## 开发环境调试
在 IntelliJ IDEA 或 Eclipse 中直接运行 `com.quality.platform.QualityPlatformApplication` 主类即可。需要确保依赖服务（MySQL、Redis、HBase）已启动并可从开发机访问。

## 常见问题
1. **启动失败：数据库连接拒绝**  
   检查 MySQL 服务是否运行，以及 `application.yml` 中的连接地址、用户名密码是否正确。
2. **Redis 连接异常**  
   确认 Redis 服务已启动，且密码（如果有）配置正确。
3. **HBase 扫描超时或表不存在**  
   确认 Zookeeper 地址正确，且 HBase 中已创建表 `realtime_alert`（列族 `cf`）。
4. **Quartz 任务未触发**  
   检查规则是否启用（`status=1`），以及 cron 表达式是否有效。

## 维护与扩展
- 如需增加新的规则类型，可在 `RuleType` 枚举中添加，并实现对应的校验逻辑（位于 `service/checker` 包）。
- 告警方式目前支持钉钉和邮件，可在 `alert` 包中扩展新的通知渠道。

