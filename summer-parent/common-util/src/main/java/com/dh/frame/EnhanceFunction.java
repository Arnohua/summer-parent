package com.dh.frame;

import java.util.Set;

/**
 * @author dinghua
 * @date 2019/9/20
 * @since v1.0.0
 */
public abstract class EnhanceFunction {

    private Set<Class<?>> classes;

    private EnhanceFunction(){}

    public EnhanceFunction(Set<Class<?>> classes){
        if(classes == null || classes.size() == 0){
            throw new RuntimeException("classes不能为空");
        }
        this.classes = classes;
    }

    public Set<Class<?>> getClasses(){
        return classes;
    }
    /**
     * 自定义支持的数据类型
     * @param str
     * @param obj1
     * @param obj2
     * @return
     */
    public abstract String enhance(String str, Object obj1, Object obj2);
}
