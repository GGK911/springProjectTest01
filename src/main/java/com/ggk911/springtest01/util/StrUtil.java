package com.ggk911.springtest01.util;

/**
 * 字符串工具类
 *
 * @author TangHaoKai
 * @version V1.0 2023/12/26 12:01
 **/
public class StrUtil {
    /**
     * UTF-16生僻字截取
     * 因其字符下标串定义不同，此方法要遍历字符串
     *
     * @param str        要截取的字符串
     * @param startIndex 我们常识认为的str中第几个字符的下标（下标从0开始，左闭右开）
     * @param endIndex   是我们常识认为的str中第几个字符的下标（下标从0开始，左闭右开）
     * @return 我们常识认为应该截取的从startIndex到endIndex的str中的子串
     */
    public static String subString(String str, int startIndex, int endIndex) {
        // 定义：str是我们常识认为的中文字符
        // startIndex是我们常识认为的str中第几个字符的下标（下标从0开始）
        // endIndex是我们常识认为的str中第几个字符的下标（下标从0开始）
        if (cn.hutool.core.util.StrUtil.isEmpty(str)) {
            return "";
        }
        // 字符串的总代码点数
        int codePointCount = str.codePointCount(0, str.length());
        // 创建一个纸带，记录每个 代码点数  所占 代码单位 大小，优化了纸带长度
        int[] tape = new int[Math.min(codePointCount, endIndex)];
        // 当前代码点数进度
        int strLength = 0;
        for (int index = 0; index <= tape.length - 1; index++) {
            // 接着判断一下 截取下来的加上后面的部分 的 代码单元和代码点数 是否一致
            String subStringNext = str.substring(strLength, (strLength + 2 > str.length() - 1 ? str.length() : strLength + 2));
            if (cn.hutool.core.util.StrUtil.isBlank(subStringNext)) {
                break;
            }
            if (subStringNext.length() == subStringNext.codePointCount(0, subStringNext.length())) {
                // // 如果一致，纸带进度+1 代码点数+1，并记录所占 代码点数 位数
                tape[index] = 1;
                strLength++;
            } else {
                // 如果不一致，则证明截取下来的部分不完整要加上后面一位才完整
                tape[index] = 2;
                strLength += 2;
            }
        }
        // startIndex真实下标
        int startSum = 0;
        for (int i = 0; i < startIndex; i++) {
            startSum += tape[i];
        }
        // endIndex真实下标
        int endSum = 0;
        for (int i = startIndex; i < endIndex; i++) {
            endSum += tape[i];
        }
        return str.substring(startSum, startSum + endSum);
    }
}
