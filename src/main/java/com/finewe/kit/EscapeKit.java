package com.finewe.kit;

import cn.hutool.core.util.EscapeUtil;
import cn.hutool.core.util.StrUtil;

public class EscapeKit extends EscapeUtil {

    /**
     * Escape编码（Unicode）<br>
     * 该方法不会对 ASCII 字母和数字进行编码，也不会对下面这些 ASCII 标点符号进行编码： * @ - _ + . / 。其他所有的字符都会被转义序列替换。

     * @param content 被转义的内容
     * @return 编码后的字符串
     */
    public static String escapeUpper(String content) {
        if(StrUtil.isBlank(content)) {
            return content;
        }
        int i;
        char j;
        StringBuilder tmp = new StringBuilder();
        tmp.ensureCapacity(content.length() * 6);

        for (i = 0; i < content.length(); i++) {

            j = content.charAt(i);

            if (Character.isDigit(j) || Character.isLowerCase(j) || Character.isUpperCase(j)) {
                tmp.append(j);
            }else if (j < 256) {
                tmp.append("%");
                if (j < 16) {
                    tmp.append("0");
                }
                tmp.append(Integer.toString(j, 16).toUpperCase());
            } else {
                tmp.append("%u");
                tmp.append(Integer.toString(j, 16).toUpperCase());
            }
        }
        return tmp.toString();
    }
}
