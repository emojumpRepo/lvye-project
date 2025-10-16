package cn.iocoder.yudao.module.psychology.controller.app.assessment.vo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 兼容 answers 既可为 JSON 数组，也可为 JSON 字符串（内容为数组）。
 */
public class AnswersDeserializer extends JsonDeserializer<List<WebAssessmentParticipateReqVO.AssessmentAnswerItem>> {

    @Override
    public List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        ObjectMapper objectMapper = (ObjectMapper) parser.getCodec();

        JsonToken currentToken = parser.getCurrentToken();
        if (currentToken == JsonToken.VALUE_NULL) {
            return Collections.emptyList();
        }

        // 情况一：直接是数组
        if (currentToken == JsonToken.START_ARRAY) {
            return objectMapper.readValue(parser,
                    objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, WebAssessmentParticipateReqVO.AssessmentAnswerItem.class));
        }

        // 情况二：是字符串，内部内容是数组的 JSON 字符串
        if (currentToken == JsonToken.VALUE_STRING) {
            String text = parser.getText();
            if (text == null || text.trim().isEmpty()) {
                return Collections.emptyList();
            }
            JsonNode node = objectMapper.readTree(text);
            if (node.isArray()) {
                return objectMapper.convertValue(node,
                        objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, WebAssessmentParticipateReqVO.AssessmentAnswerItem.class));
            }
        }

        // 其它情况：尝试通用读取（容错）
        JsonNode node = objectMapper.readTree(parser);
        if (node != null && node.isArray()) {
            return objectMapper.convertValue(node,
                    objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, WebAssessmentParticipateReqVO.AssessmentAnswerItem.class));
        }

        return Collections.emptyList();
    }
}


