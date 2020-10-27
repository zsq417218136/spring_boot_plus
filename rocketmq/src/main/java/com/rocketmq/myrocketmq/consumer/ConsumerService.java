package com.rocketmq.myrocketmq.consumer;

import java.util.List;

import com.rocketmq.myrocketmq.listener.MessageEvent;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


/**
 * @author zhaishuaiqing
 * @date 2020/10/27 10:16
 */
@Component
public class ConsumerService {
    @EventListener(condition = "#event.msgs[0].topic=='user-topic' && #event.msgs[0].tags=='white'")
    public void rocketmqMsgListener(MessageEvent event) {
        try {
            List<MessageExt> msgs = event.getMsgs();
            for (MessageExt msg : msgs) {
                System.err.println("消费消息:"+new String(msg.getBody()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

