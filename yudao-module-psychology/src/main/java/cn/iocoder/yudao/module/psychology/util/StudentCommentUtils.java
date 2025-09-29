package cn.iocoder.yudao.module.psychology.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

/**
 * 学生评语工具类
 * 
 * @author MinGoo
 */
@Slf4j
public class StudentCommentUtils {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Random RANDOM = new Random();
    
    /**
     * 从JSON数组格式的学生评语中随机选择一条
     * 
     * @param studentCommentJson JSON数组格式的学生评语，如：["评语1", "评语2", "评语3"]
     * @return 随机选择的一条评语，如果解析失败或为空则返回null
     */
    public static String selectRandomComment(String studentCommentJson) {
        if (studentCommentJson == null || studentCommentJson.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 解析JSON数组
            List<String> comments = OBJECT_MAPPER.readValue(studentCommentJson, new TypeReference<List<String>>() {});
            
            if (comments == null || comments.isEmpty()) {
                log.warn("学生评语JSON数组为空: {}", studentCommentJson);
                return null;
            }
            
            // 过滤掉空字符串
            List<String> validComments = comments.stream()
                    .filter(comment -> comment != null && !comment.trim().isEmpty())
                    .toList();
            
            if (validComments.isEmpty()) {
                log.warn("学生评语JSON数组中没有有效评语: {}", studentCommentJson);
                return null;
            }
            
            // 随机选择一条评语
            int randomIndex = RANDOM.nextInt(validComments.size());
            String selectedComment = validComments.get(randomIndex);
            
            log.debug("从{}条评语中随机选择了第{}条: {}", validComments.size(), randomIndex + 1, selectedComment);
            return selectedComment;
            
        } catch (Exception e) {
            log.error("解析学生评语JSON失败: {}", studentCommentJson, e);
            return null;
        }
    }
    
    /**
     * 验证学生评语JSON格式是否正确
     * 
     * @param studentCommentJson JSON数组格式的学生评语
     * @return 是否为有效的JSON数组格式
     */
    public static boolean isValidCommentJson(String studentCommentJson) {
        if (studentCommentJson == null || studentCommentJson.trim().isEmpty()) {
            return false;
        }
        
        try {
            List<String> comments = OBJECT_MAPPER.readValue(studentCommentJson, new TypeReference<List<String>>() {});
            return comments != null && !comments.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
