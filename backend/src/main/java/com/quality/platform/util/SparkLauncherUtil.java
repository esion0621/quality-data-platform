package com.quality.platform.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.launcher.SparkLauncher;

import java.io.IOException;

@Slf4j
public class SparkLauncherUtil {
    public static void submitJob(Integer ruleId, Long instanceId) {
        try {
            SparkLauncher launcher = new SparkLauncher()
                    .setAppResource("hdfs://master:9000/apps/spark-quality-job-1.0.0.jar")
                    .setMainClass("com.quality.spark.QualityJob")
                    .setMaster("yarn")
                    .setDeployMode("cluster")
                    .setConf("spark.executor.memory", "1g")
                    .addAppArgs(ruleId.toString(), instanceId.toString());

            Process process = launcher.launch();
            // 可记录日志
            log.info("Spark job submitted for ruleId: {}, instanceId: {}", ruleId, instanceId);
        } catch (IOException e) {
            log.error("Spark submit failed", e);
        }
    }
}
