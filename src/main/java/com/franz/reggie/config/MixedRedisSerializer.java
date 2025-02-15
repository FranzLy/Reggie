package com.franz.reggie.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import java.nio.charset.StandardCharsets;

public class MixedRedisSerializer implements RedisSerializer<Object> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object value) throws SerializationException {
        try {
            if (value instanceof String) {
                // 如果是字符串，直接保存
                return ((String) value).getBytes(StandardCharsets.UTF_8);
            } else {
                // 如果是对象，序列化成 JSON
                return objectMapper.writeValueAsBytes(value);
            }
        } catch (Exception e) {
            throw new SerializationException("Could not serialize object", e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            String data = new String(bytes, StandardCharsets.UTF_8);
            if (data.startsWith("{") || data.startsWith("[")) {
                // 可能是 JSON 数据，尝试解析
                return objectMapper.readValue(data, Object.class);
            } else {
                // 不是 JSON，则直接返回字符串
                return data;
            }
        } catch (Exception e) {
            throw new SerializationException("Could not deserialize object", e);
        }
    }
}
