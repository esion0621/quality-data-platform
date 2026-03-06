package com.quality.platform.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AlertSender {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${dingtalk.webhook}")
    private String dingtalkWebhook;

    // 你的钉钉机器人加签密钥（请从机器人设置中获取并替换）
    private static final String DINGTALK_SECRET = ""; 

    public void sendDingTalk(Integer ruleId, String abnormalValue, String thresholdValue) {
        try {
            // 1. 准备基础消息内容
            Map<String, Object> message = new HashMap<>();
            message.put("msgtype", "text");
            Map<String, String> text = new HashMap<>();
            text.put("content", String.format("数据质量告警：规则%d 异常值%s 超过阈值%s", ruleId, abnormalValue, thresholdValue));
            message.put("text", text);

            // 2. 生成时间戳和签名
            long timestamp = System.currentTimeMillis();
            String sign = generateSignature(timestamp);

            // 3. 构建带签名的URL
            String signedUrl = String.format("%s&timestamp=%d&sign=%s", dingtalkWebhook, timestamp, sign);

            // 4. 发送POST请求
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<Map<String, Object>> request = new org.springframework.http.HttpEntity<>(message, headers);

            restTemplate.postForEntity(signedUrl, request, String.class);
            log.info("DingTalk alert sent successfully for ruleId: {}", ruleId);
        } catch (Exception e) {
            log.error("Failed to send DingTalk alert", e);
        }
    }

    /**
     * 生成钉钉机器人签名
     * 参考官方文档：https://open.dingtalk.com/document/robots/customize-robot-security-settings
     */
    private String generateSignature(long timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
        String stringToSign = timestamp + "\n" + DINGTALK_SECRET;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(DINGTALK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        String sign = Base64.getEncoder().encodeToString(signData);
        return URLEncoder.encode(sign, StandardCharsets.UTF_8);
    }
}
