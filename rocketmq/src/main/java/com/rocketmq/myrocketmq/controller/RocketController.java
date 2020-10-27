package com.rocketmq.myrocketmq.controller;

import com.rocketmq.myrocketmq.config.RocketMQConfig;
import com.rocketmq.one.config.JmsConfig;
import com.rocketmq.one.producer.Producer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaishuaiqing
 * @date 2020/10/27 9:29
 */
@Slf4j
@RestController
public class RocketController {

    @Autowired
    RocketMQConfig rocketMQConfig;



    @RequestMapping("/text/myrocketmq")
    public Object callback() throws Exception {
        System.out.println(rocketMQConfig);
        return rocketMQConfig.getNamesrvAddr();
    }
}
