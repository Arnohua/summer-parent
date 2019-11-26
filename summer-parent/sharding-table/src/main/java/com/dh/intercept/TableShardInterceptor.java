package com.dh.intercept;

import com.dh.annotation.TableShardAnnotation;
import com.dh.frame.ITableShardStrategy;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Properties;

/**
 * 拦截sql 根据分表策略路由
 * @author dinghua
 * @date 2019-11-18
 * @since v1.0.0
 */
@Intercepts({
        @Signature(
                type = StatementHandler.class,
                method = "prepare",
                args = {Connection.class, Integer.class}
        )
})
public class TableShardInterceptor implements Interceptor {

    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    private static final ReflectorFactory REFLECTOR_FACTORY = new DefaultReflectorFactory();
    private static Logger logger = LoggerFactory.getLogger(TableShardInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (invocation.getTarget() instanceof RoutingStatementHandler) {
            try {
                RoutingStatementHandler statementHandler = (RoutingStatementHandler) invocation.getTarget();
                //元数据对象(MetaObject)实际上就是提供 类|集合|Map 的一种自动识别的访问形式.(有点类似于反射)
                MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, REFLECTOR_FACTORY);
                //获取sql语句
                BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");
                String originSql = boundSql.getSql();
                if (!StringUtils.isEmpty(originSql)) {
                    MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue("delegate.mappedStatement");
                    // 只针对添加了TableShard注解的方法才会去做分表处理
                    TableShardAnnotation tableShardAnnotation = getTableShardAnnotation(mappedStatement);
                    if (tableShardAnnotation != null) {
                        // 分表注解中指定的表名
                        String tableName = tableShardAnnotation.tableName();
                        // 分表路由字段
                        String[] shardParamKey = tableShardAnnotation.shardParamKey();
                        // 分表策略
                        Class<? extends ITableShardStrategy> shadeStrategy = tableShardAnnotation.shadeStrategy();
                        ITableShardStrategy tableStrategy = shadeStrategy.newInstance();
                        String newSql = tableStrategy.tableShard(metaStatementHandler, tableName, shardParamKey);
                        // 把新语句设置回去
                        metaStatementHandler.setValue("delegate.boundSql.sql", newSql);
                    }
                }
            } catch (Exception e) {
               logger.error("分表处理失败",e);
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        // 当目标类是StatementHandler类型时，才包装目标类，否者直接返回目标本身,减少目标被代理的次数
        return (target instanceof RoutingStatementHandler) ? Plugin.wrap(target, this) : target;
    }

    @Override
    public void setProperties(Properties properties) {

    }

    /**
     * 获取方法上的TableShardAnnotation注解
     * @param mappedStatement
     * @return
     */
    private TableShardAnnotation getTableShardAnnotation(MappedStatement mappedStatement) {
        TableShardAnnotation tableShardAnnotation = null;
        try {
            // id是方法的全名
            String id = mappedStatement.getId();
            int index = id.lastIndexOf(".");
            // 类名
            String className = id.substring(0, index);
            // 方法名
            String methodName = id.substring(index + 1);
            final Method[] method = Class.forName(className).getMethods();
            for (Method m : method) {
                if (m.getName().equals(methodName) && m.isAnnotationPresent(TableShardAnnotation.class)) {
                    tableShardAnnotation = m.getAnnotation(TableShardAnnotation.class);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("获取分表注解失败",e);
        }
        return tableShardAnnotation;
    }
}
