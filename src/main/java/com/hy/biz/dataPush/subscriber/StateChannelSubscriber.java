package com.hy.biz.dataPush.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hy.biz.dataPush.subscriber.handler.MessageHandler;
import com.hy.biz.dataResolver.exception.MessageParsingException;
import com.hy.config.HyConfigProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;

import java.util.List;

import static com.hy.biz.dataResolver.constants.MessageConstants.*;
import static com.hy.biz.dataResolver.util.TypeConverter.byteArrToStr;

/**
 *
 * Redis 订阅频道消息处理解析器
 *
 * @author shiwentao
 * @package com.hy.config
 * @create 2023-05-04 16:07
 **/
@Slf4j
public class StateChannelSubscriber implements MessageListener {

    private final List<MessageHandler> messageHandlers;
    private final HyConfigProperty hyConfigProperty;
    private final ObjectMapper mapper;

    public StateChannelSubscriber(HyConfigProperty hyConfigProperty, List<MessageHandler> messageHandlers, ObjectMapper mapper) {
        this.hyConfigProperty = hyConfigProperty;
        this.messageHandlers = messageHandlers;
        this.mapper = mapper;
    }

    /**
     * 该方法用于处理从订阅频道中接收到的 JSON 数据
     * 该方法先尝试将接收到的数据转换成一个 JsonNode
     * 若jsonNode中含 "data", 它将被当作一个控制报文来进行解析
     * 若jsonNode中含 "result", 它将被当作设备上下线状态报文来进行解析
     * 若jsonNode中不包含前两者，程序抛出解析异常
     */
    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {

        JsonNode rootNode;
        try {
            log.info("Redis订阅频道收到的消息（用于调试）: {}", message);
            rootNode = mapper.readTree(byteArrToStr(message.getBody()));
        } catch (JsonProcessingException e) {
            throw new MessageParsingException(JSON_PROCESSING_ERROR);
        }

        for (MessageHandler messageHandler : messageHandlers) {
            if (messageHandler.canHandle(rootNode)) {
                try {
                    messageHandler.handle(rootNode, hyConfigProperty.getDataQueue().getDnmTopicChannelPb());
                } catch (Exception e) {
                    throw new MessageParsingException(SUB_MESSAGE_PARSING_ERROR);
                }
                return;
            }
        }

        throw new MessageParsingException(ILLEGAL_SUBSCRIBED_MESSAGE_SIGNATURE_ERROR);
    }

}
