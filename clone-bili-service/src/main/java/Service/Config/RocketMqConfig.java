package Service.Config;


import Service.UserFollowingService;
import Service.WebSocketService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mysql.cj.util.StringUtils;
import domain.UserFollowing;
import domain.UserMoment;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static domain.Constant.Constants.*;

@Configuration
public class RocketMqConfig {
    @Value("${rocketmq.name.server.address}") //对应yaml
    private String nameServerAddr;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    @Autowired(required = false)
    private UserFollowingService userFollowingService;

    @Bean("momentsProducer")
    public DefaultMQProducer momentsProducer() throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer(GROUP_MOMENTS); //新建生产者、分组
        producer.setNamesrvAddr(nameServerAddr);
        producer.start();
        return producer;
    }

    @Bean("momentsConsumer")
    public DefaultMQPushConsumer momentsConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(GROUP_MOMENTS);
        consumer.setNamesrvAddr(nameServerAddr);
        consumer.subscribe(TOPIC_MOMENTS, "*"); //订阅主题与二级主题
        consumer.registerMessageListener(new MessageListenerConcurrently() { //并行处理监听器
            @Override
            //获得消息和上下文
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msg, ConsumeConcurrentlyContext context) {


                /*for (MessageExt message : msg) {
                    System.out.println(msg);
                } 一次只能发送一条消息*/
                MessageExt message = msg.get(0);
                if (message == null) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                String bodyStr = new String(message.getBody()); //byte数组转为字符串
                UserMoment userMoment = JSONObject.parseObject(bodyStr, UserMoment.class); //获取到用户动态
                Long userId = userMoment.getUserId();
                //查找user的关注者
                List<UserFollowing> userFans = userFollowingService.getUserFans(userId);
                //给每一个用户发送消息
                for (UserFollowing fan : userFans) {
                    //用redis查询
                    String key = "subscribed-" + fan.getUserId();
                    String subscribedListStr = redisTemplate.opsForValue().get(key);//获取订阅列表
                    List<UserMoment> subscribedList;
                    if (StringUtils.isNullOrEmpty(subscribedListStr)) {
                        subscribedList = new ArrayList<>();
                    } else {
                        subscribedList = JSON.parseArray(subscribedListStr, UserMoment.class); //字符串转为数列
                    }
                    subscribedList.add(userMoment);
                    redisTemplate.opsForValue().set(key, JSON.toJSONString(subscribedList));//数列转为字符串储存入redis
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }

    @Bean("bulletScreenProducer")
    public DefaultMQProducer bulletScreenProducer() throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer(GROUP_MOMENTS); //新建生产者、分组
        producer.setNamesrvAddr(nameServerAddr);
        producer.start();
        return producer;
    }

    @Bean("bulletScreenConsumer")
    public DefaultMQPushConsumer bulletScreenConsumer() throws MQClientException {
        //实例化消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(GROUP_BULLET_SCREEN);
        //设置nameserver地址
        consumer.setNamesrvAddr(nameServerAddr);
        //订阅一个或多个topic和tag过滤信息
        consumer.subscribe(TOPIC_BULLET_SCREEN, "*"); //订阅主题与二级主题
        //注册回调实现类，处理从broker拉取的信息
        consumer.registerMessageListener(new MessageListenerConcurrently() { //并行处理监听器
            @Override
            //获得消息和上下文
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {

                /*for (MessageExt message : msg) {
                    System.out.println(msg);
                } 一次只能发送一条消息*/
                MessageExt msg = msgs.get(0);
                byte[] msgByte = msg.getBody();
                String bodyStr = new String(msgByte);//byte数组转为字符串
                JSONObject jsonObject = JSONObject.parseObject(bodyStr);
                String sessionId = jsonObject.getString("sessionId");
                String message = jsonObject.getString("message");
                WebSocketService webSocketService = WebSocketService.WEBSOCKET_MAP.get(sessionId);
                if (webSocketService.getSession().isOpen()) {
                    try {
                        webSocketService.sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }

}
