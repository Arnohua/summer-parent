package com.dh.frame;

import org.apache.ibatis.reflection.MetaObject;


/**
 * @author dinghua
 * @date 2019-11-19
 */
public interface ITableShardStrategy {

    /**
     * 分表策略
     *
     * @param metaStatementHandler MetaObject包装的RoutingStatementHandler对象
     * @param tableName            原始表名
     * @param shardParamKey        可以在mapper文件的方法里面传递一些参数key过来，在分表策略里面通过key获取到对应的值
     * @return 包装之后的sql语句
     */
    String tableShard(MetaObject metaStatementHandler, String tableName, String[] shardParamKey) throws Exception;

}
