# 数据质量监控平台

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## 📖 项目简介

数据质量监控平台是一个面向大数据场景的自动化质量保障系统，支持**离线任务调度**与**实时流数据质量校验**。通过灵活的规则配置，对 HDFS 文件或 Kafka 消息进行空值、唯一性、范围等校验，并提供任务执行追踪、多维度告警、实时指标监控等能力，帮助数据团队快速发现并定位数据质量问题。

## 🖥️ 环境要求

- **操作系统**：Ubuntu 20.04 / CentOS 7 （本项目基于三节点虚拟机集群部署）
- **JDK**：17
- **Node.js**：18.x
- **Maven**：3.8+
- **Hadoop**：3.2.4（HDFS/YARN）
- **Spark**：3.1.3
- **Kafka**：3.2.0
- **MySQL**：8.0
- **Redis**：5.0.7
- **HBase**：2.4.11

## 🏗️ 技术栈

| 模块 | 技术 | 版本 |
|------|------|------|
| 前端 | React | 18.2 |
|      | Vite | 4.5 |
|      | Ant Design | 5.15 |
|      | ECharts | 5.5 |
|      | Axios | 1.6 |
| 后端 | Spring Boot | 3.2.4 |
|      | MySQL | 8.0 |
|      | Quartz | 2.3.2 |
| 实时计算 | Spark Structured Streaming | 3.1.3 |
|         | Kafka | 3.2.0 |
| 存储   | Redis | 5.0.7 |
|        | HBase | 2.4.11 |

## 🧱 系统架构

![架构图](docs/architecture.png)

- **前端**：提供规则管理、任务实例、告警记录、实时监控看板。
- **后端**：提供 REST API，管理规则元数据、任务实例和离线告警，通过 Quartz 调度离线 Spark 作业。
- **实时计算**：Spark Structured Streaming 消费 Kafka 消息，按窗口聚合指标，写入 Redis 和 HBase。
- **数据存储**：
  - MySQL：规则、任务实例、离线告警。
  - Redis：实时窗口指标（Hash 结构）。
  - HBase：实时告警明细。

## 🚀 功能模块

### 1. 数据看板
- 最近7天任务趋势图（总数、成功、失败）
- 各规则通过率柱状图（颜色分级）

### 2. 规则管理
- 规则的增删改查
- 支持 HDFS/Kafka 数据源
- 规则类型：NULL_CHECK、UNIQUE_CHECK、RANGE_CHECK、SQL_EXPRESSION
- 手动触发规则（立即执行一次）

### 3. 任务实例
- 展示所有离线任务执行记录
- 按规则 ID 筛选
- 查看任务详情（执行结果摘要、错误信息）

### 4. 告警记录
- **离线告警**：由离线任务触发，支持确认操作
- **实时告警**：由 Spark Streaming 直接写入 HBase，实时展示

### 5. 实时监控
- 实时异常率趋势图（最近20个窗口，每条规则一条线）
- 当前异常率表格（异常率颜色标识）
- 5秒轮询最新指标

## ⚙️ 配置说明

### 后端配置
文件：`backend/src/main/resources/application.yml`  
主要项（已用占位符替换）：
```yaml
spring:
  datasource:
    url: jdbc:mysql://node1:3306/quality
    username: your_mysql_username
    password: your_mysql_password
  redis:
    host: node1
    password: your_redis_password
hbase:
  zookeeper:
    quorum: node2,node3
kafka:
  bootstrap-servers: node2:9092
dingtalk:
  webhook: https://oapi.dingtalk.com/robot/send?access_token=your_token
```

### 前端配置
文件：`frontend/.env`
```
VITE_API_BASE_URL=/api   # 开发环境代理到后端
```
代理配置在 `frontend/vite.config.js` 中默认指向 `http://node1:2006`。

### 实时作业配置
文件：`spark-streaming-quality-job/src/main/resources/application.conf`
```
kafka.brokers = "node2:9092"
hbase.zookeeper.quorum = "node2,node3"
redis.host = "node1"
```

## 🚦 快速启动

### 启动后端
```bash
cd backend
mvn clean package
java -jar target/quality-platform.jar
```

### 启动前端
```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:2005
```

### 启动实时作业
（需先启动 Kafka、HBase、Redis）
```bash
cd spark-streaming-quality-job
mvn clean package
spark-submit --class com.quality.streaming.StreamingApp \
             --master yarn \
             --deploy-mode cluster \
             target/spark-streaming-quality-job-1.0.0.jar
```

### 提交离线作业（由后端自动调度，也可手动触发）
手动触发示例：
```bash
curl -X POST http://node1:2006/api/rules/trigger/{ruleId}
```

## 📁 项目结构
```
.
├── backend                      # Spring Boot 后端
│   ├── src/main/java            # 源代码
│   └── pom.xml
├── frontend                     # React 前端
│   ├── src
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── spark-quality-job            # 离线 Spark 作业
│   ├── src
│   └── pom.xml
└── spark-streaming-quality-job  # 实时 Spark Streaming 作业
    ├── src
    └── pom.xml
```

## 📸 截图

将以下截图文件放置在 `docs/` 目录下：

| 文件名 | 说明 |
|--------|------|
| `dashboard.png` | 数据看板页面 |
| `rules.png`     | 规则管理页面 |
| `tasks.png`     | 任务实例页面 |
| `alerts.png`    | 告警记录页面（离线+实时）|
| `realtime.png`  | 实时监控页面 |

## 📄 许可证
[Apache License 2.0](LICENSE)
```

---

### 图片存放说明
在项目根目录创建 `docs/` 文件夹，并将上述截图放入该文件夹。README 中的图片链接就会正确显示。例如：`![架构图](docs/architecture.png)` 需要你手动放置 `architecture.png` 文件。
