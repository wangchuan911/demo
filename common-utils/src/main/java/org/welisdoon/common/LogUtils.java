package org.welisdoon.common;

/**
 * @Classname LogUtils
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/17 09:30
 */
public interface LogUtils {
    static String format(CharSequence header, CharSequence... bodys) {
        int max = header.length(), lineMax;
        for (CharSequence body : bodys) {
            max = Math.max(body.length(), max);
        }
        if (max == header.length()) {
            max += 10;
        }

        StringBuilder lineSp = new StringBuilder(),
                builder = new StringBuilder("\n");
        formatLine(max, header, '#', builder);
        for (CharSequence body : bodys) {
            formatLine(max, body, ' ', builder);
        }
        formatLine(max, "", '#', builder);
        return builder.toString();
    }

    static void formatLine(int max, CharSequence content, char sp, StringBuilder builder) {
        int lineMax = (max - content.length()) / 2;
        StringBuilder lineSp = new StringBuilder();
        for (int i = 0; i < lineMax; i++) {
            lineSp.append(sp);
        }
        builder.append(lineSp).append(content).append(lineSp).append("\n");
    }

    /**
     * @Param pattern 前面的图案 such as "=============="
     * @Param code 颜色代号：背景颜色代号(41-46)；前景色代号(31-36)
     * @Param n 数字+m：1加粗；3斜体；4下划线
     * @Param content 要打印的内容
     */
    static String styleString(String patten, int color, int n, String content) {
        //  "%s\33[%d;%dm%s\33[0m %n"
        return String.format("%s\33[%d;%dm%s\33[0m", patten, color, n, content);
    }
}
