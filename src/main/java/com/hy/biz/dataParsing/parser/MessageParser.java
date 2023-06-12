package com.hy.biz.dataParsing.parser;

import com.hy.biz.dataParsing.dto.BaseMessage;
import com.hy.biz.dataParsing.dto.MessageDTO;
import com.hy.biz.dataParsing.dto.WaveDataMessage;
import com.hy.biz.dataParsing.exception.MessageParsingException;
import com.hy.biz.dataParsing.parser.strategy.MessageParserStrategy;
import com.hy.biz.dataParsing.registry.MessageClassRegistry;
import com.hy.biz.dataParsing.registry.MessageStrategyRegistry;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.util.Map;

import static com.hy.biz.dataParsing.constants.MessageConstants.ILLEGAL_MESSAGE_SIGNATURE_ERROR;
import static com.hy.biz.dataParsing.constants.MessageConstants.UNABLE_TO_PARSE;
import static com.hy.biz.dataParsing.util.TypeConverter.hexStringToByteArray;
import static java.nio.ByteOrder.BIG_ENDIAN;


/**
 *==========================================
 * 解析器：用于解析 Redis 阻塞队列中的报文数据    ｜
 *==========================================
 *
 * @author shiwentao
 * @package com.hy.biz.dataParsing
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
        MessageDTO messageDTO = parserHelper.createMessageDTO(data);  //生成报文DTO

        String commandData = messageDTO.getData().getCommand();       //拿到报文字节数组
        long timeStamp = messageDTO.getTimeStamp();                   //拿到时间戳

        ByteBuffer buffer = ByteBuffer.wrap(hexStringToByteArray(commandData)).order(BIG_ENDIAN);

        BaseMessage unoccupiedSpecificMsg;
        BaseMessage specificMsg;
        do {
            parserHelper.checkHeader(buffer);
            byte frameType = parserHelper.parseFrameType(buffer);
            byte messageType = parserHelper.parseMessageType(buffer);

            unoccupiedSpecificMsg = createUnoccupiedSpecificMsg(frameType, messageType);
            specificMsg = parseMessageContent(unoccupiedSpecificMsg, buffer, timeStamp);

            parserHelper.checkSum(buffer);
        } while (unoccupiedSpecificMsg instanceof WaveDataMessage && buffer.hasRemaining());

        return specificMsg;
    }


    //===========================private=================================private=========================================private================================

    /**
     * @param messageType 报文类型
     * @return 具体报文实体类
     * @description 该方法通过报文签名key，在 MESSAGE_MAP 中找到对应的报文实体类，并进行创建初始化
     */
    private BaseMessage createUnoccupiedSpecificMsg(byte frameType, byte messageType) {

        Class<? extends BaseMessage> messageClass = MESSAGE_MAP.get(String.format("%s:%s", frameType, messageType));
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
    private BaseMessage parseMessageContent(BaseMessage specificMessage, ByteBuffer buffer, long timeStamp) {

        byte[] messageContent = parserHelper.parseMessageContent(buffer);

        ByteBuffer contentBuffer = ByteBuffer.wrap(messageContent).order(BIG_ENDIAN);

        MessageParserStrategy strategy =
                MESSAGE_STRATEGY_MAP.get(specificMessage.getFrameType() + ":" + specificMessage.getMessageType());
        if (strategy != null)
            try {
                return strategy.parse(contentBuffer, specificMessage, timeStamp);
            } catch (Exception e) {
                throw new MessageParsingException(UNABLE_TO_PARSE);
            }

        throw new MessageParsingException(ILLEGAL_MESSAGE_SIGNATURE_ERROR);
    }


}


