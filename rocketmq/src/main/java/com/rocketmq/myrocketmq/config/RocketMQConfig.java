package com.rocketmq.myrocketmq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaishuaiqing
 * @date 2020/10/27 9:25
 */
@Component
@PropertySource(value = "classpath:config/rocketmq.properties")
@ConfigurationProperties(prefix ="rocketmq")
@Data
public class RocketMQConfig {
    private String namesrvAddr;
    private String producerGroupName;
    private String transactionProducerGroupName;
    private String consumerGroupName;
    private String producerInstanceName;
    private String consumerInstanceName;
    private String producerTranInstanceName;
    private int consumerBatchMaxSize;
    private boolean consumerBroadcasting;
    private boolean enableHistoryConsumer;
    private boolean enableOrderConsumer;
    private List<String> subscribe = new ArrayList<String>();

}
