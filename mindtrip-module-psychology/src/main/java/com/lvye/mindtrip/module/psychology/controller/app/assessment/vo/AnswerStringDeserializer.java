package com.lvye.mindtrip.module.psychology.controller.app.assessment.vo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
 

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 兼容 AssessmentAnswerItem.answer 字段既可为字符串，也可为字符串数组。
 * 数组场景下会以逗号拼接为单个字符串；空数组返回 null。
 */
public class AnswerStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonNode node = parser.readValueAsTree();

        if (node == null || node.isNull()) {
            return "";
        }

        if (node.isTextual()) {
            return node.asText();
        }

        if (node.isArray()) {
            List<String> parts = new ArrayList<>();
            for (JsonNode child : node) {
                if (child == null || child.isNull()) {
                    continue;
                }
                parts.add(child.asText());
            }
            if (parts.isEmpty()) {
                return "";
            }
            return String.join(",", parts);
        }

        // 其它类型（如数字、布尔、对象）统一转为文本
        return node.asText();
    }
}


