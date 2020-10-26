package com.rocketmq.one.config;

/**
 * @author zhaishuaiqing
 * @date 2020/10/26 17:07
 */
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
@PropertySource("classpath:config/rocketmq.properties")
@ConfigurationProperties(prefix = "rocketmq")
@Configuration
@Data
public class RocketMQProperties {
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
