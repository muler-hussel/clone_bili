package Service;

import Service.Util.RocketMqUtil;
import Service.Util.TokenUtil;
import com.alibaba.fastjson2.JSONObject;
import domain.BulletScreen;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static domain.Constant.Constants.TOPIC_BULLET_SCREEN;

@Service
@Component
@ServerEndpoint("/imserver/{token}")
public class WebSocketService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //当前客户端数量。原子性操作的类
    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);

    //并发哈希，保存每个客户端的WebSocketService
    public static final ConcurrentHashMap<String, WebSocketService> WEBSOCKET_MAP = new ConcurrentHashMap<>();

    private Session session;

    public Session getSession() {
        return session;
    }

    public String getSessionId() {
        return sessionId;
    }

    private String sessionId;

    private Long userId;

    //多例websocketService共用
    private static ApplicationContext APPLICATION_CONTEXT;
    public static void setApplicationContext(ApplicationContext applicationContext) {
        WebSocketService.APPLICATION_CONTEXT = applicationContext;
    }

    @OnOpen //连接成功后应该调用的方法
    public void openConnection(Session session, @PathParam("token") String token) {
        this.sessionId = session.getId();
        this.session = session;
        try {
            this.userId = TokenUtil.verifyToken(token);
        } catch (Exception e){}
        if (WEBSOCKET_MAP.containsKey(sessionId)) {
            WEBSOCKET_MAP.remove(sessionId);
            WEBSOCKET_MAP.put(sessionId, this);
        } else {
            WEBSOCKET_MAP.put(sessionId, this);
            ONLINE_COUNT.getAndIncrement(); //在线人数加一
        }
        logger.info(sessionId + "connected successfully." + " Current users: " + ONLINE_COUNT.get());
        try {
            this.sendMessage("0"); //返回前端状态码0
        } catch (Exception e) {
            logger.error("Connection abnormal");
        }
    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    @OnClose
    public void closeConnection() {
        if (WEBSOCKET_MAP.containsKey(sessionId)) {
            WEBSOCKET_MAP.remove(sessionId);
            ONLINE_COUNT.getAndDecrement();
        }
        logger.info(sessionId + "exits." + " Current users: " + ONLINE_COUNT.get());
    }

    //定时推送在线人数
    @Scheduled(fixedRate = 5000)
    private void noticeOnlineCount() throws IOException {
        for (Map.Entry<String, WebSocketService> entry : WEBSOCKET_MAP.entrySet()) {
            WebSocketService webSocketService = entry.getValue();
            if (webSocketService.session.isOpen()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("onlineCount", ONLINE_COUNT.get());
                jsonObject.put("msg", "Current user: " + ONLINE_COUNT.get());
                webSocketService.sendMessage(jsonObject.toJSONString());
            }
        }
    }
    @OnMessage
    public void onMessage(String message) {
        logger.info("User: " + sessionId + ", message: " + message);
        if (!StringUtil.isNullOrEmpty(message)) {
            try {
                //群发消息
                for (Map.Entry<String, WebSocketService> entry : WEBSOCKET_MAP.entrySet()) {
                    WebSocketService webSocketService = entry.getValue();
                    //请求过多，不能直接发送。放入mq
                    //获取生产者
                    DefaultMQProducer bulletScreenProducer = (DefaultMQProducer) APPLICATION_CONTEXT.getBean("bulletScreenProducer");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("message", message);
                    jsonObject.put("sessionId", webSocketService.getSessionId());
                    //消息构建
                    Message msg = new Message(TOPIC_BULLET_SCREEN, jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
                    //异步发送消息
                    RocketMqUtil.asyncSendMsg(bulletScreenProducer, msg);
                }
                //保存弹幕到数据库、redis
                if (this.userId != null) {
                    //数据库
                    BulletScreen bulletScreen = JSONObject.parseObject(message, BulletScreen.class);
                    bulletScreen.setUserId(userId);
                    bulletScreen.setCreateTime(new Date());
                    BulletScreenService bulletScreenService = (BulletScreenService)APPLICATION_CONTEXT.getBean(BulletScreenService.class);
                    //异步保存到数据库
                    bulletScreenService.asyncAddBulletScreen(bulletScreen);
                    //也可以引入mq削峰保存 todo
                    //redis。能支持的并发量远大于数据库，可以同步保存。
                    bulletScreenService.addBulletScreenToRedis(bulletScreen);
                }

            } catch (Exception e) {
                logger.error("Bullet screen receiving fails");
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void OnError(Throwable error) {

    }
}
