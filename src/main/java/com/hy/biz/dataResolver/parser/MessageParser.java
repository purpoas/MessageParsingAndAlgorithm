package com.hy.biz.dataResolver.parser;

import com.hy.biz.dataResolver.dto.BaseMessage;
import com.hy.biz.dataResolver.dto.MessageDTO;
import com.hy.biz.dataResolver.dto.WaveDataMessage;
import com.hy.biz.dataResolver.exception.MessageParsingException;
import com.hy.biz.dataResolver.parser.strategy.MessageParserStrategy;
import com.hy.biz.dataResolver.registry.MessageClassRegistry;
import com.hy.biz.dataResolver.registry.MessageStrategyRegistry;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.util.Map;

import static com.hy.biz.dataResolver.constants.MessageConstants.ILLEGAL_MESSAGE_SIGNATURE_ERROR;
import static com.hy.biz.dataResolver.util.TypeConverter.hexStringToByteArray;
import static java.nio.ByteOrder.BIG_ENDIAN;


/**
 *
 * 解析器：用于解析 Redis 阻塞队列中的报文数据
 *
 * @author shiwentao
 * @package com.hy.biz.dataResolver
 * @create 2023-05-08 16:12
 **/
@Component
@Transactional
@Slf4j
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MessageParser {

    private final Map<String, Class<? extends BaseMessage>> MESSAGE_MAP = MessageClassRegistry.getMessageMap();
    private final Map<String, MessageParserStrategy> MESSAGE_STRATEGY_MAP = MessageStrategyRegistry.getMessageStrategyMap();

    private final ParserHelper parserHelper;

    public MessageParser(ParserHelper parserHelper) {
        this.parserHelper = parserHelper;
    }

    /**
     * @param data json字符串
     * @return 具体报文类型
     */
    public BaseMessage parse(String data) {

        MessageDTO messageDTO = parserHelper.createMessageDTO(data);

        String commandData = messageDTO.getData().getCommand();

        ByteBuffer buffer = ByteBuffer.wrap(hexStringToByteArray(commandData)).order(BIG_ENDIAN);

        BaseMessage specificMessage;
        BaseMessage messageParsed;
        do {
            parserHelper.checkHeader(buffer);
            byte frameType = parserHelper.parseFrameType(buffer);
            byte messageType = parserHelper.parseMessageType(buffer);

            specificMessage = createSpecificMsg(frameType, messageType);
            messageParsed = parseMessageContent(specificMessage, buffer);

            parserHelper.checkSum(buffer);
        } while (specificMessage instanceof WaveDataMessage && buffer.hasRemaining());

        return messageParsed;
    }


    // 私有方法==========================================================================================================

    /**
     * @param messageType 报文类型
     * @return 具体报文实体类
     * @description 该方法通过报文签名key，在 MESSAGE_MAP 中找到对应的报文实体类，并进行创建初始化
     */
    private BaseMessage createSpecificMsg(byte frameType, byte messageType) {

        Class<? extends BaseMessage> messageClass = MESSAGE_MAP.get(frameType + ":" + messageType);
        BaseMessage specificMessage;
        try {
            specificMessage = messageClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new MessageParsingException(ILLEGAL_MESSAGE_SIGNATURE_ERROR);
        }

        specificMessage.setFrameType(frameType);
        specificMessage.setMessageType(messageType);

        return specificMessage;
    }


    /**
     * @param specificMessage 具体报文类型
     * @return 被持久化的报文实体类
     * @description 该方法根据收到的报文类型，调用负责解析该类型的解析方法
     */
    private BaseMessage parseMessageContent(BaseMessage specificMessage, ByteBuffer buffer) {

        byte[] messageContent = parserHelper.parseMessageContent(buffer);

        ByteBuffer contentBuffer = ByteBuffer.wrap(messageContent).order(BIG_ENDIAN);

        MessageParserStrategy strategy =
                MESSAGE_STRATEGY_MAP.get(specificMessage.getFrameType() + ":" + specificMessage.getMessageType());
        if (strategy != null)
            return strategy.parse(contentBuffer, specificMessage);

        throw new MessageParsingException(ILLEGAL_MESSAGE_SIGNATURE_ERROR);

    }


}


