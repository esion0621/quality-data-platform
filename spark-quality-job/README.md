# Spark Quality Job

质量校验 Spark 作业，由后端调度触发，对 HDFS 数据源执行规则校验，并将结果回写至 MySQL。

## 构建
```bash
mvn clean package
```
打包后生成 `target/spark-quality-job-1.0.0.jar`。

## 运行
```bash
spark-submit --class com.quality.spark.QualityJob \
             --master yarn \
             --deploy-mode cluster \
             target/spark-quality-job-1.0.0.jar <ruleId> <instanceId>
```

### 参数说明
- `<ruleId>`: 规则ID（对应 MySQL `quality_rule` 表）
- `<instanceId>`: 任务实例ID（对应 MySQL `quality_task_instance` 表，用于回调更新状态）

## 配置
作业运行时需从后端获取规则详情，因此需要确保 `application.conf` 或命令行参数中指定了后端 API 地址（例如 `api.base.url=http://node1:2006/api`）。配置方式参见 `src/main/resources/application.conf`。

## 注意事项
- 作业完成后会调用后端回调接口 `/api/callback/spark-job` 更新任务状态。
- 需提前将 `hdfs-site.xml`、`core-site.xml` 等 Hadoop 配置文件放置在 Spark 类路径下，或通过 `--files` 分发。

