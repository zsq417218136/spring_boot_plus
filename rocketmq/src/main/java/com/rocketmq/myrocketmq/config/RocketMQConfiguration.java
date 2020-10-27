package com.rocketmq.myrocketmq.config;

import com.rocketmq.myrocketmq.listener.MessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhaishuaiqing
 * @date 2020/10/27 10:03
 */
@Slf4j
@Configuration
public class RocketMQConfiguration {
    @Autowired
    private RocketMQConfig rocketMQConfig;

    //事件监听
    @Autowired
    private ApplicationEventPublisher publisher = null;

    private static boolean isFirstSub = true;

    private static long startTime = System.currentTimeMillis();

    /**
     * 容器初始化的时候 打印参数
     */
    @PostConstruct
    public void init() {
        System.err.println(rocketMQConfig.getNamesrvAddr());
        System.err.println(rocketMQConfig.getProducerGroupName());
        System.err.println(rocketMQConfig.getConsumerBatchMaxSize());
        System.err.println(rocketMQConfig.getConsumerGroupName());
        System.err.println(rocketMQConfig.getConsumerInstanceName());
        System.err.println(rocketMQConfig.getProducerInstanceName());
        System.err.println(rocketMQConfig.getProducerTranInstanceName());
        System.err.println(rocketMQConfig.getTransactionProducerGroupName());
        System.err.println(rocketMQConfig.isConsumerBroadcasting());
        System.err.println(rocketMQConfig.isEnableHistoryConsumer());
        System.err.println(rocketMQConfig.isEnableOrderConsumer());
        System.out.println(rocketMQConfig.getSubscribe().get(0));
    }


    /**
     * 创建普通消息发送者实例
     * @return
     * @throws MQClientException
     */
    @Bean
    public DefaultMQProducer defaultProducer() throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer(
                rocketMQConfig.getProducerGroupName());
        producer.setNamesrvAddr(rocketMQConfig.getNamesrvAddr());
        producer.setInstanceName(rocketMQConfig.getProducerInstanceName());
        producer.setVipChannelEnabled(false);
        producer.setRetryTimesWhenSendAsyncFailed(10);
        producer.start();
        log.info("rocketmq producer server is starting....");
        return producer;
    }

    /**
     * 创建支持消息事务发送的实例
     * @return
     * @throws MQClientException
     */
    @Bean
    public TransactionMQProducer transactionProducer() throws MQClientException {
        TransactionMQProducer producer = new TransactionMQProducer(
                rocketMQConfig.getTransactionProducerGroupName());
        producer.setNamesrvAddr(rocketMQConfig.getNamesrvAddr());
        producer.setInstanceName(rocketMQConfig
                .getProducerTranInstanceName());
        producer.setRetryTimesWhenSendAsyncFailed(10);
        // 事务回查最小并发数
        producer.setCheckThreadPoolMinSize(2);
        // 事务回查最大并发数
        producer.setCheckThreadPoolMaxSize(2);
        // 队列数
        producer.setCheckRequestHoldMax(2000);
        producer.start();
        log.info("rocketmq transaction producer server is starting....");
        return producer;
    }

    /**
     * 创建消息消费的实例
     * @return
     * @throws MQClientException
     */
    @Bean
    public DefaultMQPushConsumer pushConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(
                rocketMQConfig.getConsumerGroupName());
        consumer.setNamesrvAddr(rocketMQConfig.getNamesrvAddr());
        consumer.setInstanceName(rocketMQConfig.getConsumerInstanceName());

        //判断是否是广播模式
        if (rocketMQConfig.isConsumerBroadcasting()) {
            consumer.setMessageModel(MessageModel.BROADCASTING);
        }
        //设置批量消费
        consumer.setConsumeMessageBatchMaxSize(rocketMQConfig
                .getConsumerBatchMaxSize() == 0 ? 1 : rocketMQConfig
                .getConsumerBatchMaxSize());

        //获取topic和tag
        List<String> subscribeList = rocketMQConfig.getSubscribe();
        for (String sunscribe : subscribeList) {
            consumer.subscribe(sunscribe.split(":")[0], sunscribe.split(":")[1]);
        }
        // 顺序消费
        if (rocketMQConfig.isEnableOrderConsumer()) {
            consumer.registerMessageListener(new MessageListenerOrderly() {
                @Override
                public ConsumeOrderlyStatus consumeMessage(
                        List<MessageExt> msgs, ConsumeOrderlyContext context) {
                    try {
                        context.setAutoCommit(true);
                        msgs = filterMessage(msgs);
                        if (msgs.size() == 0){
                            return ConsumeOrderlyStatus.SUCCESS;
                        }
                        publisher.publishEvent(new MessageEvent(msgs, consumer));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                    }
                    return ConsumeOrderlyStatus.SUCCESS;
                }
            });
        } else {
            // 并发消费
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(
                        List<MessageExt> msgs,
                        ConsumeConcurrentlyContext context) {
                    try {
                        //过滤消息
                        msgs = filterMessage(msgs);
                        if (msgs.size() == 0) {
                            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                        }
                        publisher.publishEvent(new MessageEvent(msgs, consumer));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }

                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    try {
                        consumer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    log.info("rocketmq consumer server is starting....");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return consumer;
    }

    /**
     * 消息过滤
     * @param msgs
     * @return
     */
    private List<MessageExt> filterMessage(List<MessageExt> msgs) {
        if (isFirstSub && !rocketMQConfig.isEnableHistoryConsumer()) {
            msgs = msgs.stream()
                    .filter(item -> startTime - item.getBornTimestamp() < 0)
                    .collect(Collectors.toList());
        }
        if (isFirstSub && msgs.size() > 0) {
            isFirstSub = false;
        }
        return msgs;
    }

}
