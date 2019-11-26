/*
 * Copyright (c) 2016 4PX Information Technology Co.,Ltd. All rights reserved.
 */
package com.dh.common.util;

import com.alibaba.fastjson.JSON;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * @author jiangqiub
 * @date 2018年5月30日
 */
public class TestHelper {

    /**
     * 字符数组，包括数字，大小写字母
     */
    private static final char[] charArray = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    /**
     * 大写字符
     */
    private static final String[] CAPITAL_ARRAY = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    /**
     * 随机字符串的最小长度
     */
    private static final int RANDOM_STRING_MIN_LEGTH = 2;

    /**
     * 随机字符串的最大长度
     */
    private static final int RANDOM_STRING_MAX_LEGTH = 5;

    /**
     * 生成随机日期的格式，该格式精确到秒，不包含微妙
     */
    private static final String RANDOM_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 输出的xml中的日期格式
     */
    private static final String OUT_XML_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 在生成字段或者输入xml字符串时，默认忽略的属性值，如版本号属性
     */
    private static final List<String> DEFAULT_IGNORE_FIELDS = Arrays.asList("serialVersionUID");

    /**
     * 根据Class类型创建实例
     * @param clazz 需要创建的类型
     * @param ignoreFields 需要忽略设置取值的属性名称列表，该参数中的取值为实体属性的名称，处于该字段名数组中的属性将不会自动生成随机值
     *                     使用场景：
     *                     1. 对于那些不需要赋值的字段进行过滤；
     *                     2. 该工具方法默认不支持属性为实体对象的，可以通过该参数进行过滤
     * @param overrideFieldMap 需要重载的属性map， key为实体属性名称；value为该属性的取值；
     *                         使用场景：
     *                         1. 因为有可能随机生成的属性取值不满足需求，可以使用该参数对属性取值进行指定
     *                         2. 该工具方法为字段生成随机值时，遇到不支持的属性类型时，将抛出异常；如果想继续使用该工具方法，可以使用该参数，进行人为指定不支持的类型属性的取值
     * @param isIgnoreSuperClassField 是否需要为父类中的属性进行赋值；true表示忽略父类中的字段；false表示需要为父类中的字段进行赋值
     *                                默认true
     * @param <T> 类型变量
     * @return
     */
    public static <T> T newInstance(Class<T> clazz, String[] ignoreFields, OverrideFieldMap<String, Object> overrideFieldMap, boolean isIgnoreSuperClassField,T data){
        try{
            T instance = clazz.newInstance();
            setValueForIntance(instance, clazz, ignoreFields, overrideFieldMap, isIgnoreSuperClassField,data);
            printDataAndCode(instance, ignoreFields);
            return instance;
        }catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 为实例按照类的继承关系由子类到父类进行递归属性赋值
     * <p>注意该方法默认会忽略父类中的属性，只为当前class类的属性赋予随机值
     * <p>该方法不支持内部嵌套实体对象，如果内部存在实体对象属性，则可以使用ignoreFields参数进行忽略
     *
     * @param instance 实例
     * @param clazz 类或者父类
     * @param ignoreFields 不需要赋值的属性名称列表
     * @param overrideFieldMap 需要人为指定取值的属性名称及值map
     * @param isIgnoreSuperClassField 是否需要忽略父类中的属性；true表示忽略为父类中的属性进行赋值；false反之
     * @throws IllegalAccessException
     */
    private static void setValueForIntance(Object instance, Class clazz, String[] ignoreFields, OverrideFieldMap<String, Object> overrideFieldMap, boolean isIgnoreSuperClassField,Object data) throws IllegalAccessException {
        // 通过反射获取字段列表
        Field[] fields = clazz.getDeclaredFields();
        if (fields != null && fields.length > 0){
            // 遍历字段为字段设置随机值
            for (Field f : fields){
                f.setAccessible(true);
                // 判断字段是否为需要忽略的字段
                if (isIgnoreField(ignoreFields, f)){
                    continue;
                }

                // 判断是否为需要重载的字段
                if (isOverrideField(overrideFieldMap, f)){
                    // 如果是需要重载取值的字段，则使用提供的重载值
                    f.set(instance, overrideFieldMap.get(f.getName()));
                }else{
                    // 否则，如果不是需要重载取值的字段，则根据类型生成随机值
                    f.set(instance, f.get(data));
                }
            }
        }

        // 判断是否需要忽略父类中的字段，若忽略，则父类中的字段取值都为空
        if(isIgnoreSuperClassField){
            return;
        }else{
            // 如果不忽略父类中的字段，则获取父类型，级联设置字段的取值
            Class supperClass = clazz.getSuperclass();
            if (supperClass == null || isObjectClass(supperClass)){
                return;
            }else{
                setValueForIntance(instance, supperClass, ignoreFields, overrideFieldMap, isIgnoreSuperClassField,data);
            }
        }
    }

    /**
     * 根据class类型创建实例,
     * <p>注意该方法默认会忽略父类中的属性，只为当前class类的属性赋予随机值</p>
     * <p>该方法不支持内部嵌套实体对象，如果内部存在实体对象属性，则可以使用ignoreFields参数进行忽略</p>
     * <p>不支持内部属性包含集合或者数组的对象，如果存在该类型的字段，则可以使用ignoreFields参数进行忽略</p>
     *
     * @param clazz 需要创建的类型
     * @param ignoreFields 需要忽略设置取值的属性名称列表，该参数中的取值为实体属性的名称，处于该字段名数组中的属性将不会自动生成随机值
     *                     使用场景：
     *                     1. 对于那些不需要赋值的字段进行过滤；
     *                     2. 该工具方法默认不支持属性为实体对象的，可以通过该参数进行过滤
     * @param overrideFields 需要重载的属性map， key为实体属性名称；value为该属性的取值；
     *                         使用场景：
     *                         1. 因为有可能随机生成的属性取值不满足需求，可以使用该参数对属性取值进行指定
     *                         2. 该工具方法为字段生成随机值时，遇到不支持的属性类型时，将抛出异常；如果想继续使用该工具方法，可以使用该参数，进行人为指定不支持的类型属性的取值
     * @param <T> 类型变量
     * @return
     */
    public static <T> T newInstance(Class<T> clazz, String[] ignoreFields, OverrideFieldMap<String, Object> overrideFields,T data)  {
        T obj = newInstance(clazz, ignoreFields, overrideFields, true,data);
        printDataAndCode(obj, ignoreFields);
        return obj;
    }

    /**
     * 打印输出生成的xml字符串及json代码
     * @param ignoreFields
     * @param obj 数据对象
     * @param <T>
     */
    private static <T> void printDataAndCode(T obj, String[] ignoreFields) {
        Class<T> clazz  = (Class<T>)obj.getClass();
        System.out.println("------------------------------------------------------------------");
        System.out.println(toDbTestXml(obj, ignoreFields, true));
        String jsonStr = JSON.toJSONString(obj);

        // 构造代码
        jsonStr = jsonStr.replaceAll("\"", "\\\\\"");
        String fieldName = String.valueOf(clazz.getSimpleName().charAt(0)).toLowerCase() + clazz.getSimpleName().substring(1);
        String jsonFieldName = fieldName + "Json";
        String className = clazz.getSimpleName();
        System.out.println("String " + jsonFieldName + " = \"" + jsonStr + "\";");
        System.out.println( className + " " + fieldName + " = JSON.parseObject(" + jsonFieldName + ", " + className + ".class); ");
    }

    /**
     * 根据字段类型生成随机值
     * @param field
     * @return
     */
    private static Object getValueByType(Field field) {
        String typeSimpleName = field.getType().getSimpleName();
        if (isChar(field)){
            return randomChar();
        } else if (isPrimitiveShort(field) || isWrapShort(field)) {
            return randomShort();
        }else if (isPrimitiveInt(field) || isWrapInteger(field)) {
            return randomInt();
        }else if (isPrimitiveLong(field) || isWrapLong(field)) {
            return randomLong();
        }else if (isPrimitiveFloat(field) || isWrapFloat(field)) {
            return randomFloat();
        }else if (isPrimitiveDouble(field) || isWrapDouble(field)) {
            return randomDouble();
        }else if (isBigDecimal(field)) {
            return randomBigDecimal();
        }else if (isString(field)) {
            return randomString(RANDOM_STRING_MIN_LEGTH, RANDOM_STRING_MAX_LEGTH);
        }else if (isPrimitiveBoolean(field) || isWrapBoolean(field)) {
            return randomBoolean();
        }else if (isDate(field)) {
            return randomDate();
        }else {
            throw new RuntimeException("不支持的类型: " + typeSimpleName);
        }
    }

    /**
     * 根据字段名称获取数据库中相应字段的名称，
     * <p> 字段名称必须符合驼峰命名规则，
     * <p> 生成的数据库表字段名称以下划线分隔
     * @param fieldName
     * @return
     */
    private static String getDbColumnNameByFieldName(String fieldName) {
        StringBuilder sb = new StringBuilder();
        List<String> capitals = Arrays.asList(CAPITAL_ARRAY);
        for (int i = 0; i < fieldName.length(); i++) {
            char s = fieldName.charAt(i);
            if (capitals.contains(String.valueOf(s))) {
                sb.append("_").append(String.valueOf(s).toLowerCase());
            }else {
                sb.append(String.valueOf(s));
            }
        }
        return sb.toString();
    }

    /**
     * 根据实体类名获取数据库表名称
     * <p> 实体名称必须符合驼峰命名规则
     * <p> 生成的数据库表名，单词之间以下划线"_"分隔
     *
     * @param className
     * @return
     */
    private static String getTableNameByClassName(String className) {
        StringBuilder sb = new StringBuilder();
        List<String> capitals = Arrays.asList(CAPITAL_ARRAY);
        for (int i = 0; i < className.length(); i++) {
            char s = className.charAt(i);
            if (capitals.contains(String.valueOf(s))) {
                if (i == 0) {
                    sb.append(String.valueOf(s).toLowerCase());
                }else {
                    sb.append("_").append(String.valueOf(s).toLowerCase());
                }
                
            }else {
                sb.append(String.valueOf(s));
            }
        }
        return sb.toString();
    }
    
    private static boolean isDate(Field f) {
        if (f.getType().getName().contains("util.Date")) {
            return true;
        }else {
            return false;
        }
    }
    
    private static boolean isString(Field f) {
        if (f.getType().getName().contains("String")) {
            return true;
        }else {
            return false;
        }
    }
    
    private static boolean isPrimitiveInt(Field f) {
        String typeSimpleName = f.getType().getSimpleName();
        if (typeSimpleName.contains("int")) {
            return true;
        }else {
            return false;
        }
    }
    
    private static boolean isWrapInteger(Field f) {
        String typeSimpleName = f.getType().getSimpleName();
        if (typeSimpleName.contains("Integer")) {
            return true;
        }else {
            return false;
        }
    }
    
    private static boolean isPrimitiveLong(Field f) {
        String typeSimpleName = f.getType().getSimpleName();
        if (typeSimpleName.contains("long")) {
            return true;
        }else {
            return false;
        }
    }
    
    private static boolean isWrapLong(Field f) {
        String typeSimpleName = f.getType().getSimpleName();
        if (typeSimpleName.contains("Long")) {
            return true;
        }else {
            return false;
        }
    }
    
    private static boolean isBigDecimal(Field f) {
        String typeSimpleName = f.getType().getSimpleName();
        if (typeSimpleName.contains("BigDecimal")) {
            return true;
        }else {
            return false;
        }
    }
    
    private static boolean isPrimitiveBoolean(Field f) {
        String typeSimpleName = f.getType().getSimpleName();
        if (typeSimpleName.contains("boolean")) {
            return true;
        }else {
            return false;
        }
    }

    private static boolean isChar(Field f) {
        String typeSimpleName = f.getType().getSimpleName();
        if (typeSimpleName.contains("char")) {
            return true;
        }else {
            return false;
        }
    }

    private static boolean isPrimitiveShort(Field f) {
        String typeSimpleName = f.getType().getSimpleName();
        if (typeSimpleName.contains("short")) {
            return true;
        }else {
            return false;
        }
    }

    private static boolean isWrapShort(Field f) {
        String typeSimpleName = f.getType().getSimpleName();
        if (typeSimpleName.contains("Short")) {
            return true;
        }else {
            return false;
        }
    }

    private static boolean isPrimitiveFloat(Field f) {
        String typeSimpleName = f.getType().getSimpleName();
        if (typeSimpleName.contains("float")) {
            return true;
        }else {
            return false;
        }
    }

    private static boolean isWrapFloat(Field f) {
        String typeSimpleName = f.getType().getSimpleName();
        if (typeSimpleName.contains("Float")) {
            return true;
        }else {
            return false;
        }
    }

    private static boolean isPrimitiveDouble(Field f) {
        String typeSimpleName = f.getType().getSimpleName();
        if (typeSimpleName.contains("double")) {
            return true;
        }else {
            return false;
        }
    }

    private static boolean isWrapDouble(Field f) {
        String typeSimpleName = f.getType().getSimpleName();
        if (typeSimpleName.contains("Double")) {
            return true;
        }else {
            return false;
        }
    }

    
    private static boolean isWrapBoolean(Field f) {
        String typeSimpleName = f.getType().getSimpleName();
        if (typeSimpleName.contains("Boolean")) {
            return true;
        }else {
            return false;
        }
    }

    private static char randomChar(){
        int index = (int)(Math.random() * CAPITAL_ARRAY.length);
        return CAPITAL_ARRAY[index].charAt(0);
    }

    private static short randomShort(){
        return (short)(Math.random() * Short.MAX_VALUE);
    }

    private static int randomInt() {
        return (int)(Math.random() * (Integer.MAX_VALUE/100));
    }

    private static long randomLong() {
        return (long)(Math.random() * (Integer.MAX_VALUE/10));
    }

    private static float randomFloat(){
        BigDecimal bd = new BigDecimal(Math.random() * 100000 + "");
        return bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    private static double randomDouble(){
        BigDecimal bd = new BigDecimal(Math.random() * 100000 + "");
        return bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private static BigDecimal randomBigDecimal() {
        BigDecimal bd = new BigDecimal(Math.random() * 100000 + "");
        return bd.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private static String randomString(int minLength, int maxLength) {
        int length = (int)((maxLength - minLength) * Math.random()) + minLength;
        StringBuilder sb = new StringBuilder();
        int pos = -1;
        for (int i = 0; i < length; i++) {
            pos = (int)(charArray.length * Math.random());
            sb.append(charArray[pos]);
        }
        return sb.toString();
    }

    private static boolean randomBoolean(){
        int index = (int)(Math.random() * Short.MAX_VALUE);
        if (index % 2 == 0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 生成随机日期，默认是当天，不包含毫秒值
     * @return
     * @throws ParseException
     */
    private static Date randomDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(RANDOM_DATE_FORMAT);
        String randomDateStr = sdf.format(new Date());
        try {
            return sdf.parse(randomDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期格式化输出
     * @param date
     * @return
     */
    private static String formatDate(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(OUT_XML_DATE_FORMAT);
        return sdf.format(date);
    }
    
    public static OverrideFieldMap<String, Object>  overrideFieldMap(){
        OverrideFieldMap<String, Object> overrideFieldMap = new OverrideFieldMap<String, Object>();
        return overrideFieldMap;
    }
    
    @SuppressWarnings("hiding")
    public static final class OverrideFieldMap<String, Object>{
        Map<String, Object> overrideFieldMap = new HashMap<String, Object>();
        
        public OverrideFieldMap<String, Object> put(String key, Object value) {
            overrideFieldMap.put(key, value);
            return this;
        } 
        
        public boolean containsKey(String key) {
            return overrideFieldMap.containsKey(key);
        }
        
        public Object get(String key) {
            return overrideFieldMap.get(key);
        }
    }

    /**
     * 判断字段是否为需要忽略的字段
     * @param ignoreFields 忽略字段列表
     * @param f 反射字段
     * @return true表示需要忽略的字段；false反之；
     */
    private static boolean isIgnoreField(String[] ignoreFields, Field f){
        if(DEFAULT_IGNORE_FIELDS.contains(f.getName())){
            // 默认忽略的字段，如版本号属性
            return true;
        }
        if (ignoreFields == null || ignoreFields.length == 0){
            return false;
        }
        List<String> ignoreFieldList = Arrays.asList(ignoreFields);
        if (ignoreFieldList.contains(f.getName())){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 判断字取值是否为需要重载的字段
     * @param overrideFieldMap
     * @param field
     * @return
     */
    private static boolean isOverrideField(OverrideFieldMap<String, Object> overrideFieldMap, Field field){
        if (overrideFieldMap == null){
            return false;
        }
        if (overrideFieldMap.containsKey(field.getName())){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 生成xml字符串
     * @param entity 实体对象
     * @param ignoreFields 不需要再输出的xml中出现的属性列表，可以是当前类及父类中的属性名称
     * @param isIgnoreSuperClassField  是否 忽略父类中的属性，true表示忽略，在输出的xml中不会存在父类属性; false 不忽略，xml中将会输出父类中的属性
     * @return
     */
    public static String toDbTestXml(Object entity, String[] ignoreFields, boolean isIgnoreSuperClassField){
        StringBuilder xmlSb = new StringBuilder();
        try{
            Class clazz = entity.getClass();
            String tableName = getTableNameByClassName(clazz.getSimpleName());
            xmlSb.append("<").append(tableName); // xml元素头
            List<Field> fieldList = getAllOutField(entity, ignoreFields, isIgnoreSuperClassField);
            for (Field f : fieldList){
                Object value = f.get(entity);
                if (value != null){
                    // 格式化日期
                    if(isDate(f)){
                        value = formatDate((Date)value);
                    }
                    xmlSb.append(" ").append(getDbColumnNameByFieldName(f.getName())).append("=").append("\"").append(value).append("\"");
                }
            }
            xmlSb.append(">").append("</").append(tableName).append(">"); // xml元素尾
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return xmlSb.toString();
    }
    
    /**
     * 生成xml字符串（父类中的属性将不会输出到xml内容中）
     * @param entity 实体对象
     * @param ignoreFields 不需要再输出的xml中出现的属性列表，可以是当前类及父类中的属性名称
     * @return
     */
    public static String toDbTestXml(Object entity, String[] ignoreFields) {
        return toDbTestXml(entity, ignoreFields, true);
    }

    
    
    /**
     * 获取所有输出的字段列表
     * @param entity
     * @param ignoreFields
     * @param isIgnoreSuperClassField
     * @return
     */
    private static List<Field> getAllOutField(Object entity, String[] ignoreFields, boolean isIgnoreSuperClassField){
        List<Field> outFieldList = new ArrayList<>();
        List<Field> fieldList = new ArrayList<>();
        getChildAndSuperClassField(fieldList, entity.getClass(), isIgnoreSuperClassField);
        for (Field f : fieldList){
            f.setAccessible(true);
            if (!isIgnoreField(ignoreFields, f)){
                outFieldList.add(f);
            }
        }
        return outFieldList;
    }

    /**
     * 获取当前类及父类中的所有字段列表
     * @param fieldList
     * @param clazz
     * @param isIgnoreSuperClassField
     */
    private static void getChildAndSuperClassField(List<Field> fieldList, Class clazz, boolean isIgnoreSuperClassField){
        Field[] fields = clazz.getDeclaredFields();
        if (fields != null){
            fieldList.addAll(Arrays.asList(fields));
        }
        if (!isIgnoreSuperClassField){
            Class superClass = clazz.getSuperclass();
            if(superClass != null && !isObjectClass(superClass) ){
                getChildAndSuperClassField(fieldList, superClass, isIgnoreSuperClassField);
            }else{
                return;
            }
        }else{
            return;
        }
    }

    /**
     * 判断是否为Object 类
     * @param clazz
     * @return
     */
    private static boolean isObjectClass(Class clazz){
        return "java.lang.Object".equals(clazz.getName());
    }
    
}
