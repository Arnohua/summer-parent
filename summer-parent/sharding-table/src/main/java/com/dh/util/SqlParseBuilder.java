package com.dh.util;

/**
 * @author dinghua
 * @date 2020/9/16
 * @since v1.0.0
 */
public class SqlParseBuilder {

    private static final char LINE_END = '\n';

    private static final char SPACE = ' ';

    private static final char PARENTHESIS = '(';

    private static final char POINT = ';';

    /**
     * 耗时更小
     * @param sql
     * @param target
     * @param replacement
     * @return
     */
    public static String parse(String sql,String target,String replacement){
        if (sql == null || target == null || replacement == null) {
            return sql;
        }
        int idx = sql.indexOf(target);
        if (idx == -1) {
            return sql;
        }
        int replacementLength = replacement.length();
        int targetLength = target.length();
        while (idx != -1){
            if(idx == 0){
                idx = sql.indexOf(target,idx + targetLength);
                continue;
            }
            char c1 = sql.charAt(idx - 1);
            char c2 = sql.charAt(idx + targetLength);
            if(isReplace(c1,c2)){
                sql = sql.substring(0,idx).concat(replacement).concat(sql.substring(idx + targetLength));
                idx = sql.indexOf(target,idx + replacementLength);
            } else {
                idx = sql.indexOf(target,idx + targetLength);
            }
        }
        return sql;
    }

    public static String parseV2(String sql,String target,String replacement){
        if (sql == null || target == null || replacement == null) {
            return sql;
        }
        int start = sql.indexOf(target);
        if (start == -1) {
            return sql;
        }
        char[] src = sql.toCharArray();
        int offset = 0;
        final StringBuilder builder = new StringBuilder();
        int targetLength = target.length();
        while(start > -1){
            if(start == 0){
                start = sql.indexOf(target,start + targetLength);
                if(start == -1){
                    return sql;
                }
                builder.append(src,0,start);
                continue;
            } else {
                builder.append(src,offset,(start - offset));
            }
            offset = start;
            char c1 = src[start - 1];
            char c2 = src[start + targetLength];
            if(isReplace(c1,c2)){
                builder.append(replacement);
                offset += targetLength;
            }
            start = sql.indexOf(target,start + targetLength);
        }
        int length = sql.length();
        if(offset < length){
            builder.append(src,offset, (length - offset));
        }
        return builder.toString();
    }

    private static boolean isReplace(char c1,char c2){
        return (LINE_END == c1 || SPACE == c1)
                && (LINE_END == c2 || SPACE == c2 || PARENTHESIS == c2 || POINT == c2);
    }



}
