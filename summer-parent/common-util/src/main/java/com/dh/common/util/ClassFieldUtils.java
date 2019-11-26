package com.dh.common.util;


import com.dh.annotation.FieldCompare;
import com.dh.frame.EnhanceFunction;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 用于比较类中字段值的变更，默认支持String，Integer，Byte，Long，Double，Float，Short，
 * BigDecimal，BigInteger，Boolean，Date，String[]几种类型数据的比较，
 * 也可以自定义需要比较的数据类型
 * @author dinghua
 * @date 2019/9/4
 * @since v1.0.0
 */
public class ClassFieldUtils {

    private static final String SPLIT_STR = ":";

    private static final String CONCAT_STR = " -> ";

    private static final String SKIP_STR = " </br> ";

    private static final Logger logger = LoggerFactory.getLogger(ClassFieldUtils.class);

    private static Set<Class<?>> classSet = new HashSet<>();

    /** 支持的数据类型*/
    static{
        classSet.add(String.class);
        classSet.add(Integer.class);
        classSet.add(Byte.class);
        classSet.add(Long.class);
        classSet.add(Double.class);
        classSet.add(Float.class);
        classSet.add(Short.class);
        classSet.add(BigDecimal.class);
        classSet.add(BigInteger.class);
        classSet.add(Boolean.class);
        classSet.add(Date.class);
        classSet.add(String[].class);
    }

    /**
     * 比较clazz类中字段值的变更
     * 支持基本数据类型及String数组字段的比较
     * @param clazz
     * @param source
     * @param target
     * @return
     */
    public static String compareFieldValue(Class<?> clazz, Object source, Object target){
        return compareFieldValue(clazz,source,target,null);
    }

    /**
     * 比较clazz类中字段值的变更
     * 支持基本数据类型及String数组字段的比较（数字类型的字段会处理精度问题）
     * @param clazz
     * @param source
     * @param target
     * @return
     */
    public static String compareFieldValueWithNumberPrecision(Class<?> clazz, Object source, Object target){
        return compareFieldValueWithNumberPrecision(clazz,source,target,null);
    }

    /**
     * 比较clazz类中字段值的变更
     * compareFieldValueWithPrecision方法增强，支持自定义数据类型(数字类型的字段会处理精度问题)
     * @param clazz
     * @param source
     * @param target
     * @param enhanceFunction
     * @return
     */
    public static String compareFieldValueWithNumberPrecision(Class<?> clazz, Object source, Object target, EnhanceFunction enhanceFunction){
        source = dealWithFieldNumberPrecision(clazz,source);
        target = dealWithFieldNumberPrecision(clazz,target);
        return compareFieldValue(clazz,source,target,enhanceFunction);
    }

    /**
     * 比较clazz类中字段值的变更
     * compareFieldValue方法增强，支持自定义数据类型
     * @param clazz
     * @param source
     * @param target
     * @param enhanceFunction
     * @return
     */
    public static String compareFieldValue(Class<?> clazz, Object source, Object target, EnhanceFunction enhanceFunction) {
       if (source == null || target == null){
           return StringUtils.EMPTY;
       }
        // 获取本类所有字段包含父类
        Field[] fields = new Field[0];
        for (; clazz != Object.class; clazz = clazz.getSuperclass()){
            Field[] superFields = clazz.getDeclaredFields();
            fields = ArrayUtils.addAll(fields,superFields);
        }
        StringBuffer sb = new StringBuffer();
        for (Field field : fields){
            // 非基本数据类型不支持比较
            Class<?> type = field.getType();
            if (!isBaseDataType(type) && !(enhanceFunction != null && enhanceFunction.getClasses().contains(type))){
                continue;
            }
            // 字段比较结果
            String result = compareFieldValue(field, source, target,enhanceFunction);
            if (StringUtils.isNotEmpty(result)){
                sb.append(result).append(SKIP_STR);
            }
        }
        String result = sb.toString();
        // 结果展示处理，过滤null
        return result.replace("null"," ");
    }


    /**
     * 处理数字精度
     * @param clazz
     * @param obj
     * @return
     */
    public static Object dealWithFieldNumberPrecision(Class<?> clazz,Object obj){
        if(obj == null){
            return null;
        }
        // 获取本类所有字段包含父类
        Field[] fields = new Field[0];
        for (; clazz != Object.class; clazz = clazz.getSuperclass()){
            Field[] superFields = clazz.getDeclaredFields();
            fields = ArrayUtils.addAll(fields,superFields);
        }
        for (Field field : fields){
            FieldCompare annotation = field.getAnnotation(FieldCompare.class);
            if(annotation == null){
                continue;
            }
            int scale = annotation.scale();
            Class<?> type = field.getType();
            try {
                if(type.equals(BigDecimal.class) || type.equals(Float.class) || type.equals(Double.class)) {
                    field.setAccessible(true);
                    Object o = field.get(obj);
                    if(o == null){
                        continue;
                    }
                    BigDecimal bigDecimal = new BigDecimal(o.toString());
                    if(type.equals(BigDecimal.class)){
                        field.set(obj, bigDecimal.setScale(scale,BigDecimal.ROUND_HALF_UP));
                    } else if(type.equals(Double.class)){
                        field.set(obj,bigDecimal.setScale(scale,BigDecimal.ROUND_HALF_UP).doubleValue());
                    }else if(type.equals(Float.class)){
                        field.set(obj,bigDecimal.setScale(scale,BigDecimal.ROUND_HALF_UP).floatValue());
                    }
                }
            } catch (IllegalAccessException e) {
               logger.error("transfer number fail ",e);
            }
        }
        return obj;
    }

    /**
     * 单个字段值的比较
     * @param field
     * @param source
     * @param target
     * @return
     */
    private static String compareFieldValue(Field field, Object source,Object target,EnhanceFunction enhanceFunction){
        FieldCompare annotation = field.getAnnotation(FieldCompare.class);
        if (annotation == null){
            return StringUtils.EMPTY;
        }
        Class<?> type = field.getType();
        Object result = getResult(field, source);
        Object resultNew = getResult(field, target);
        if (result == null && resultNew == null){
            return StringUtils.EMPTY;
        }
        StringBuffer sb = new StringBuffer();
        if (result == null || resultNew == null){
            if(enhanceFunction != null && enhanceFunction.getClasses().contains(type)){
                return enhanceFunction.enhance(annotation.value(),result,resultNew);
            }
            if (type.equals(String[].class)){
                result = Arrays.toString((String[]) result);
                resultNew = Arrays.toString((String[]) resultNew);
            }
            if(type.equals(Date.class)){
                result = parseDate((Date) result);
                resultNew = parseDate((Date) resultNew);
            }
            return sb.append(annotation.value())
                     .append(SPLIT_STR)
                     .append(result)
                     .append(CONCAT_STR)
                     .append(resultNew)
                     .toString();
        }
        if (enhanceFunction != null && enhanceFunction.getClasses().contains(type)) {
            return enhanceFunction.enhance(annotation.value(), result, resultNew);
        }
        else if (type.equals(Date.class) && ((Date)result).getTime() != ((Date)resultNew).getTime()){
            return sb.append(annotation.value())
                     .append(SPLIT_STR)
                     .append(parseDate((Date) result))
                     .append(CONCAT_STR)
                     .append(parseDate((Date) resultNew))
                     .toString();
        }
        else if (type.equals(String[].class)) {
            String[] results = (String[]) result;
            String[] resultsNew = (String[]) resultNew;
            if (!compareValueForArrays(results,resultsNew)){
                return sb.append(annotation.value())
                         .append(SPLIT_STR)
                         .append(Arrays.toString(results))
                         .append(CONCAT_STR)
                         .append(Arrays.toString(resultsNew))
                         .toString();
            }
        }
        else {
            Method[] declaredMethods = type.getDeclaredMethods();
            for (Method method : declaredMethods){
                String name = method.getName();
                if ("equals".equals(name)){
                    boolean flag = true;
                    try {
                        flag = (boolean) method.invoke(result, resultNew);
                    } catch (Exception e) {
                        logger.error("field value compare fail",e);
                    }
                    if (!flag){
                        return sb.append(annotation.value())
                                 .append(SPLIT_STR)
                                 .append(result)
                                 .append(CONCAT_STR)
                                 .append(resultNew)
                                 .toString();
                    }
                }
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * 字符串数组比较
     * @param str1
     * @param str2
     * @return
     */
    private static boolean compareValueForArrays(String[] str1,String[] str2){
        return StringUtils.equals(Arrays.toString(str1),Arrays.toString(str2));
    }


    /**
     * 获取字段的值
     * @param field
     * @param source
     * @return
     */
    private static Object getResult(Field field, Object source) {
        Class<?> clazz = source.getClass();
        try {
            return getResult(clazz,field,source);
        } catch (Exception e){
            logger.error("Get field value fail",e);
        }
        return null;
    }

    /**
     * 获取字段的值
     * @param clazz
     * @param field
     * @param source
     * @return
     * @throws Exception
     */
    private static Object getResult(Class<?> clazz,Field field, Object source)throws Exception{
        field.setAccessible(true);
        PropertyDescriptor pd = new PropertyDescriptor(field.getName(), clazz);
        Method readMethod = pd.getReadMethod();
        return readMethod.invoke(source);
    }

    /**
     * 判断一个类是否为基本数据类型。
     * @param clazz 要判断的类。
     * @return true 表示为基本数据类型。
     */
    private static boolean isBaseDataType(Class clazz){
        return classSet.contains(clazz);
    }


    /**
     * 日期格式化
     * @param date
     * @return
     */
    private static String parseDate(Date date){
        if(date == null){
            return StringUtils.EMPTY;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            logger.error("Date format transform exception",e);
        }
        return date.toString();
    }
}
