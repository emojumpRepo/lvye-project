package com.lvye.mindtrip.module.psychology.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * LoginTypeEnum 测试类
 */
public class LoginTypeEnumTest {

    @Test
    public void testEnumValues() {
        // 测试枚举值是否正确
        assertEquals(0, LoginTypeEnum.STUDENT.getType());
        assertEquals(1, LoginTypeEnum.PARENT.getType());
    }

    @Test
    public void testValueOfType() {
        // 测试根据类型值获取枚举
        assertEquals(LoginTypeEnum.STUDENT, LoginTypeEnum.valueOfType(0));
        assertEquals(LoginTypeEnum.PARENT, LoginTypeEnum.valueOfType(1));
        assertNull(LoginTypeEnum.valueOfType(2));
        assertNull(LoginTypeEnum.valueOfType(null));
    }

    @Test
    public void testToIsParent() {
        // 测试枚举转Boolean
        assertFalse(LoginTypeEnum.toIsParent(LoginTypeEnum.STUDENT));
        assertTrue(LoginTypeEnum.toIsParent(LoginTypeEnum.PARENT));
        assertFalse(LoginTypeEnum.toIsParent((LoginTypeEnum) null));

        // 测试类型值转Boolean
        assertFalse(LoginTypeEnum.toIsParent(0));
        assertTrue(LoginTypeEnum.toIsParent(1));
        assertFalse(LoginTypeEnum.toIsParent(2));
        assertFalse(LoginTypeEnum.toIsParent((Integer) null));
    }

    @Test
    public void testFromIsParent() {
        // 测试Boolean转枚举
        assertEquals(LoginTypeEnum.STUDENT, LoginTypeEnum.fromIsParent(false));
        assertEquals(LoginTypeEnum.PARENT, LoginTypeEnum.fromIsParent(true));
        assertEquals(LoginTypeEnum.STUDENT, LoginTypeEnum.fromIsParent(null));
    }

    @Test
    public void testConversionConsistency() {
        // 测试转换的一致性
        // 学生：0 -> false -> STUDENT -> 0
        assertEquals(LoginTypeEnum.STUDENT, LoginTypeEnum.fromIsParent(LoginTypeEnum.toIsParent(0)));
        assertEquals(Integer.valueOf(0), LoginTypeEnum.fromIsParent(LoginTypeEnum.toIsParent(0)).getType());

        // 家长：1 -> true -> PARENT -> 1
        assertEquals(LoginTypeEnum.PARENT, LoginTypeEnum.fromIsParent(LoginTypeEnum.toIsParent(1)));
        assertEquals(Integer.valueOf(1), LoginTypeEnum.fromIsParent(LoginTypeEnum.toIsParent(1)).getType());
    }

    @Test
    public void testTargetAudienceConversion() {
        // 测试 LoginTypeEnum 转 targetAudience（现在统一了：0=学生，1=家长）
        assertEquals(Integer.valueOf(0), LoginTypeEnum.toTargetAudience(LoginTypeEnum.STUDENT));
        assertEquals(Integer.valueOf(1), LoginTypeEnum.toTargetAudience(LoginTypeEnum.PARENT));
        assertEquals(Integer.valueOf(0), LoginTypeEnum.toTargetAudience((LoginTypeEnum) null));

        // 测试 type 值转 targetAudience（现在直接返回原值）
        assertEquals(Integer.valueOf(0), LoginTypeEnum.toTargetAudience(0));
        assertEquals(Integer.valueOf(1), LoginTypeEnum.toTargetAudience(1));
        assertEquals(Integer.valueOf(2), LoginTypeEnum.toTargetAudience(2)); // 非标准值直接返回
        assertEquals(Integer.valueOf(0), LoginTypeEnum.toTargetAudience((Integer) null));
    }

    @Test
    public void testFromTargetAudience() {
        // 测试 targetAudience 转 LoginTypeEnum（现在统一了：0=学生，1=家长）
        assertEquals(LoginTypeEnum.STUDENT, LoginTypeEnum.fromTargetAudience(0));
        assertEquals(LoginTypeEnum.PARENT, LoginTypeEnum.fromTargetAudience(1));
        assertEquals(LoginTypeEnum.STUDENT, LoginTypeEnum.fromTargetAudience(2)); // 非标准值默认为学生
        assertEquals(LoginTypeEnum.STUDENT, LoginTypeEnum.fromTargetAudience(null));
    }

    @Test
    public void testTargetAudienceToIsParent() {
        // 测试 targetAudience 转 Boolean（现在统一了：0=学生，1=家长）
        assertFalse(LoginTypeEnum.targetAudienceToIsParent(0));
        assertTrue(LoginTypeEnum.targetAudienceToIsParent(1));
        assertFalse(LoginTypeEnum.targetAudienceToIsParent(2)); // 非标准值默认为学生
        assertFalse(LoginTypeEnum.targetAudienceToIsParent(null));
    }

    @Test
    public void testTargetAudienceConsistency() {
        // 测试 targetAudience 转换的一致性（现在统一了：0=学生，1=家长）
        // 学生：STUDENT -> 0 -> STUDENT
        assertEquals(LoginTypeEnum.STUDENT, LoginTypeEnum.fromTargetAudience(LoginTypeEnum.toTargetAudience(LoginTypeEnum.STUDENT)));

        // 家长：PARENT -> 1 -> PARENT
        assertEquals(LoginTypeEnum.PARENT, LoginTypeEnum.fromTargetAudience(LoginTypeEnum.toTargetAudience(LoginTypeEnum.PARENT)));

        // 学生：0 -> false -> STUDENT -> 0
        assertEquals(Integer.valueOf(0), LoginTypeEnum.toTargetAudience(LoginTypeEnum.fromTargetAudience(0)));

        // 家长：1 -> true -> PARENT -> 1
        assertEquals(Integer.valueOf(1), LoginTypeEnum.toTargetAudience(LoginTypeEnum.fromTargetAudience(1)));
    }
}
