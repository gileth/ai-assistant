package com.ganlansi.chat.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;

import com.ganlansi.chat.conf.LocalCache;
import com.ganlansi.chat.enums.ReturnCodeEnum;
import com.ganlansi.chat.exception.BusinessException;
import com.ganlansi.chat.listener.OpenAIWebSocketEventSourceListener;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
public class SocketIoService
{

    // 用来存已连接的客户端
    private static Map<String, SocketIOClient> clientMap = new ConcurrentHashMap<>();

    @Autowired
    private SocketIOServer socketIOServer;
    private static OpenAiStreamClient openAiStreamClient;

    @Autowired
    public void setOrderService(OpenAiStreamClient openAiStreamClient) {
        this.openAiStreamClient = openAiStreamClient;
    }

    //在线总数
    private static int onlineCount;
    //当前会话
    private Session session;
    //用户id -目前是按浏览器随机生成
    private String uid;

    private static CopyOnWriteArraySet<SocketIoService> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 用来存放每个客户端对应的WebSocketServer对象
     */
    private static ConcurrentHashMap<String, SocketIoService> webSocketMap = new ConcurrentHashMap();

    /**
     * 为了保存在线用户信息，在方法中新建一个list存储一下【实际项目依据复杂度，可以存储到数据库或者缓存】
     */
    private final static List<Session> SESSIONS = Collections.synchronizedList(new ArrayList<>());

    /**
     * Spring IoC容器创建之后，在加载SocketIOServiceImpl Bean之后启动
     *
     * @throws Exception
     */
    @PostConstruct
    private void autoStartup() throws Exception
    {
        start();
    }

    /**
     * Spring IoC容器在销毁SocketIOServiceImpl Bean之前关闭,避免重启项目服务端口占用问题
     *
     * @throws Exception
     */
    @PreDestroy
    private void autoStop() throws Exception
    {
        stop();
    }



    /**
     * 新客户端连接
     * @param client
     */
    private  void newCollection(SocketIOClient client){
        HandshakeData handshakeData = client.getHandshakeData();
        String userId = handshakeData.getSingleUrlParam("userId");
        String tokenId = handshakeData.getSingleUrlParam("tokenId");

        log.info("监听到有客户端接入，接入的userId 为 ： {}, sessionId为：{}，client信息为{}, namespace : {}", userId, tokenId, client, client.getNamespace().getName());

//        clientMap.put(tokenId, client);


    }

    /**
     * 客户端断开连接
     * @param client
     */
    private void disconnect(SocketIOClient client){
        String token = client.get("uid");

            log.info("不删除client,接受到客户端断开连接事件token:{}",token);
//            disconnectClient(token);
//                    log.info("完成客户端断开连接事件token:{},client:{}",token,client);

//        log.info("token为空，断联");
//        disconnectClient(client.getSessionId().toString());
    }

    /**
     * 接收到客户端消息
     * @param client
     * @param msg
     * @param ackSender
     */
    private void chatevent(SocketIOClient client, String msg, AckRequest ackSender){
        log.info("[连接ID:{}] 收到消息:{}", this.uid, msg);
        if (msg == null) {
            BusinessException.throwMessage(ReturnCodeEnum.SYSTEM_REQ_PARAM_ILLEGAL);
            //TODO
        }
        //接受参数
        OpenAIWebSocketEventSourceListener eventSourceListener = new OpenAIWebSocketEventSourceListener(this.session);
        String messageContext = (String) LocalCache.CACHE.get(uid);
        List<Message> messages = new ArrayList<>();
        if (StrUtil.isNotBlank(messageContext)) {
            messages = JSONUtil.toList(messageContext, Message.class);
            if (messages.size() >= 10) {
                messages = messages.subList(1, 10);
            }
            Message currentMessage = Message.builder().content(msg).role(Message.Role.USER).build();
            messages.add(currentMessage);
        } else {
            Message currentMessage = Message.builder().content(msg).role(Message.Role.USER).build();
            messages.add(currentMessage);
        }
        openAiStreamClient.streamChatCompletion(messages, eventSourceListener);
        LocalCache.CACHE.put(uid, JSONUtil.toJsonStr(messages), LocalCache.TIMEOUT);
    }

    public void start()
    {
        try {
            // 监听客户端连接
            socketIOServer.addConnectListener(client -> {
                newCollection(client);
            });

            // 监听客户端断开连接
            socketIOServer.addDisconnectListener(client -> {
                disconnect(client);
            });

            // 页面查询请求接收，包括首次进入页面查询、条件筛选查询、翻页查询、排序查询
            socketIOServer.addEventListener("chatevent", String.class, (client, data, ackSender) -> {
                chatevent(client, data, ackSender);
            });

            socketIOServer.start();
            log.info("socket.io初始化服务完成");
        }catch (BusinessException e){
            log.error(" start() BusinessException: "+ ReturnCodeEnum.SYSTEM_EXCEPTION,e);
        }catch(Throwable er){
            log.error("start() OtherException: "+ReturnCodeEnum.SYSTEM_EXCEPTION,er);
        }

    }

    public void stop()
    {
        if (socketIOServer != null)
        {
            socketIOServer.stop();
            socketIOServer = null;
        }
        log.info("socket.io服务已关闭");
    }

    public void disconnectClient(String token){
        try{
            SocketIOClient socket = clientMap.get(token);
            if(socket != null){
                socket.sendEvent("server-disconnect");
                socket.disconnect();
            }
        }catch(Throwable e){
            log.error("disconnectClient="+token,e);
        }
        clientMap.remove(token);
    }

    /**
     * 获取当前连接数
     *
     * @return
     */
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    /**
     * 当前连接数加一
     */
    public static synchronized void addOnlineCount() {
        SocketIoService.onlineCount++;
    }

    /**
     * 当前连接数减一
     */
    public static synchronized void subOnlineCount() {
        SocketIoService.onlineCount--;
    }

}
