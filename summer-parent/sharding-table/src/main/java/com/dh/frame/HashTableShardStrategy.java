package com.dh.frame;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.springframework.util.CollectionUtils;
import java.util.LinkedList;
import java.util.Map;


/**
 * 根据字段hash分表
 * @author dinghua
 * @date 2019-11-19
 * @since v1.0.0
 */
public class HashTableShardStrategy implements ITableShardStrategy {

    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    private static final ReflectorFactory REFLECTOR_FACTORY = new DefaultReflectorFactory();

    /**
     *
     * @param metaStatementHandler MetaObject包装的RoutingStatementHandler对象
     * @param tableName            原始表名
     * @param shardParamKey        分表策略的key
     * @return
     * @throws Exception
     */
    @Override
    public String tableShard(MetaObject metaStatementHandler, String tableName, String[] shardParamKey) throws Exception {
        BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");
        String originSql = boundSql.getSql();
        if (shardParamKey == null || shardParamKey.length == 0) {
            return originSql;
        }
        String shardParam = shardParamKey[0];
        if(shardParam == null || shardParam.equals("")){
            return originSql;
        }
        String hashShardingKey = null;
        //获取参数
        Object parameterObject = metaStatementHandler.getValue("delegate.boundSql.parameterObject");
        if (parameterObject instanceof String) {
            // 如果参数是String类型，就使用这个参数分表
            hashShardingKey = (String) parameterObject;
        } else if (parameterObject instanceof Map) {
            // 如果参数是一个Map，从map中取出参数
            Map<String, Object> map = (Map<String, Object>) parameterObject;
            for (String key : map.keySet()) {
                if (shardParam.equals(key)) {
                    hashShardingKey = map.get(shardParam).toString();
                    break;
                }
            }
        } else {
            // 参数为对象
            MetaObject metaParamObject = MetaObject.forObject(parameterObject, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, REFLECTOR_FACTORY);
            hashShardingKey = (String) metaParamObject.getValue(shardParam);
        }
        // 确定表名字
        if (hashShardingKey != null) {
            int hashCode = hashShardingKey.hashCode();
            LinkedList<String> shardingTables = TableRule.getShardingTableByTableName(tableName);
            if(CollectionUtils.isEmpty(shardingTables)){
                throw new RuntimeException("未到表名为" + tableName + "的分表");
            }
            String newTableName = shardingTables.get(hashCode % shardingTables.size());
            return originSql.replaceAll(tableName, newTableName);
        }
        return originSql;
    }
}
