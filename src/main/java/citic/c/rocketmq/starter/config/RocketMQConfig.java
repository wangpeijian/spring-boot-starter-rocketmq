package citic.c.rocketmq.starter.config;

import citic.c.rocketmq.starter.config.bindings.Bindings;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@ConfigurationProperties(prefix = "rocket-mq")
@Data
public class RocketMQConfig {

    // 设置 TCP 接入域名（此处以公共云生产环境为例）
    private String onsAddress;

    // AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
    private String accessKey;

    // SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
    private String secretKey;

    //绑定的生产者和消费者信息
    private Bindings bindings;

    /**
     * 创建基础配置信息
     *
     * @return
     */
    private Properties getBaseProperties() {
        Properties properties = new Properties();
        // AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.AccessKey, this.accessKey);
        // SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.SecretKey, this.secretKey);
        // 设置 TCP 接入域名（此处以公共云生产环境为例）
        properties.put(PropertyKeyConst.ONSAddr, this.onsAddress);
        return properties;
    }

    /**
     * 创建生产者配置信息
     *
     * @param producerId
     * @return
     */
    public Properties getProducerProperties(String producerId) {
        Properties properties = this.getBaseProperties();
        properties.setProperty(PropertyKeyConst.ProducerId, producerId);
        return properties;
    }

    /**
     * 创建消费者配置信息
     *
     * @param consumerId
     * @return
     */
    public Properties getConsumerProperties(String consumerId) {
        Properties properties = this.getBaseProperties();
        properties.setProperty(PropertyKeyConst.ConsumerId, consumerId);
        return properties;
    }
}
