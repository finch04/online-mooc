package com.tianji.trade.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LongListDeserializer extends JsonDeserializer<List<Long>> {
    @Override
    public List<Long> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode node = codec.readTree(p);

        List<Long> result = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode element : node) {
                if (element.isNumber()) {
                    result.add(element.asLong()); // 强制转换为 Long
                } else if (element.isTextual()) {
                    try {
                        result.add(Long.parseLong(element.asText()));
                    } catch (NumberFormatException e) {
                        // 处理格式错误
                        throw new IOException("Failed to parse Long: " + element.asText(), e);
                    }
                }
            }
        }
        return result;
    }
}