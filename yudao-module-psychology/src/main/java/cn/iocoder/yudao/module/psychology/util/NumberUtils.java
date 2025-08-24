package cn.iocoder.yudao.module.psychology.util;

import lombok.SneakyThrows;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-23
 * @Description:数值计算工具类
 * @Version: 1.0
 */
public class NumberUtils {

    @SneakyThrows
    public static int calculateAge(String birthDate){
        // 1. 验证日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false); // 严格模式
        Date birthDay = sdf.parse(birthDate); // 解析失败会抛出ParseException

        // 2. 计算年龄
        Calendar now = Calendar.getInstance();
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDay);

        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--; // 未过生日则年龄减1
        }
        return age;
    }

    /**
     * 判断目标值是否在指定范围内（包含边界）
     * @param target 要判断的数值
     * @param lower 范围下限
     * @param upper 范围上限
     * @return 是否在范围内
     */
    public static boolean isBetween(int target, int lower, int upper) {
        return target >= lower && target <= upper;
    }

}
