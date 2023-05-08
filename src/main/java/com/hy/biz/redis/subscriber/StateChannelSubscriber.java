package com.hy.biz.redis.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hy.biz.parser.MessageParser;
import com.hy.biz.parser.entity.dto.DeviceOnlineStatusDTO;
import com.hy.biz.parser.entity.dto.MessageDTO;
import com.hy.biz.parser.exception.MessageParsingException;
import com.hy.domain.DeviceOnlineStatus;
import com.hy.repository.DeviceOnlineStatusRepository;
import com.hy.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;

import java.time.Instant;

import static com.hy.biz.parser.util.TypeConverter.byteArrToStr;

/**
 * @author shiwentao
 * @package com.hy.config
 * @description
 * @create 2023-05-04 16:07
 **/
@Slf4j
public class StateChannelSubscriber implements MessageListener {
    private final MessageParser messageParser;
    private final DeviceOnlineStatusRepository deviceOnlineStatusRepository;
    private final DeviceRepository deviceRepository;
    private final ObjectMapper mapper;

    public StateChannelSubscriber(MessageParser messageParser, DeviceOnlineStatusRepository deviceOnlineStatusRepository, DeviceRepository deviceRepository, ObjectMapper mapper) {
        this.messageParser = messageParser;
        this.deviceOnlineStatusRepository = deviceOnlineStatusRepository;
        this.deviceRepository = deviceRepository;
        this.mapper = mapper;
    }

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        JsonNode rootNode;
        try {
            log.info("Redis订阅频道收到的消息（用于调试）: {}", message);
            rootNode = mapper.readTree(byteArrToStr(message.getBody()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (rootNode.has("data")) { //控制报文
            MessageDTO messageDTO = mapper.convertValue(rootNode, MessageDTO.class);
            handleMessage(messageDTO);
            return;
        }

        if (rootNode.has("result")) { //设备在线状态
            DeviceOnlineStatusDTO deviceOnlineStatusDTO = mapper.convertValue(rootNode, DeviceOnlineStatusDTO.class);
            handleDeviceOnlineStatus(deviceOnlineStatusDTO);
            return;
        }

        throw new MessageParsingException("无法识别订阅频道收到的消息类型（目前只支持解析设备上线通知，及控制报文）");
    }

    private void handleMessage(MessageDTO messageDTO) {
        messageParser.parse(messageDTO.getTimeStamp(), messageDTO.getData().getCommand(), messageDTO.getDeviceCode());
    }

    private void handleDeviceOnlineStatus(DeviceOnlineStatusDTO deviceOnlineStatusDTO) {
        DeviceOnlineStatus deviceOnlineStatus = new DeviceOnlineStatus();

        String[] msg = deviceOnlineStatusDTO.getResult().getMsg().split(" ");
        deviceOnlineStatus.setDeviceId(deviceRepository.findDeviceIdByCode(msg[0]));
        deviceOnlineStatus.setCollectionTime(Instant.now());
        deviceOnlineStatus.setMessage(deviceOnlineStatusDTO.getResult().getMsg());
        deviceOnlineStatus.setStatus(deviceOnlineStatusDTO.getResult().getStatus());

        deviceOnlineStatusRepository.save(deviceOnlineStatus);
    }

}
