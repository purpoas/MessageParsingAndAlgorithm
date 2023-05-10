package com.hy.biz.redis.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hy.biz.parser.handler.MessageHandler;
import com.hy.biz.parser.exception.MessageParsingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.hy.biz.parser.constants.MessageConstants.ILLEGAL_SUBSCRIBED_MESSAGE_SIGNATURE_ERROR;
import static com.hy.biz.parser.util.TypeConverter.byteArrToStr;

/**
 * @author shiwentao
 * @package com.hy.config
 * @description
 * @create 2023-05-04 16:07
 **/
@Slf4j
public class StateChannelSubscriber implements MessageListener {
    private final List<MessageHandler> messageHandlers;
    private final ObjectMapper mapper;

    public StateChannelSubscriber(List<MessageHandler> messageHandlers, ObjectMapper mapper) {
        this.messageHandlers = messageHandlers;
        this.mapper = mapper;
    }

    /**
     * This method processes the incoming message.
     * It first tries to convert the message to a JsonNode.
     * If the message has "data", it treats it as a control message and processes it accordingly.
     * If the message has "result", it treats it as a device online status message and processes it accordingly.
     * If the message doesn't fall into any of the above categories, it throws a MessageParsingException.
     */
    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {

        JsonNode rootNode;
        try {
            log.info("Redis订阅频道收到的消息（用于调试）: {}", message);
            rootNode = mapper.readTree(byteArrToStr(message.getBody()));
        } catch (JsonProcessingException e) {
            throw new MessageParsingException("Error processing the JSON", e);
        }

        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);

        for (MessageHandler messageHandler : messageHandlers) {
            if (messageHandler.canHandle(rootNode)) {
                try {
                    messageHandler.handle(rootNode, channel);
                } catch (Exception e) {
                    throw new MessageParsingException("Error processing the message by "
                            + messageHandler.getClass().getSimpleName(), e);
                }
                return;
            }
        }

        throw new MessageParsingException(ILLEGAL_SUBSCRIBED_MESSAGE_SIGNATURE_ERROR);
    }

}
