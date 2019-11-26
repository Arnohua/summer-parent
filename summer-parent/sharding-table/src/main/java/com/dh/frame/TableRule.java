package com.dh.frame;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分表规则存储类
 * @author dinghua
 * @date 2019/11/18
 * @since v1.0.0
 */
public class TableRule {

    private static Map<String,String> tableShardingRule = new ConcurrentHashMap<>(64);

    private static Map<String,LinkedList<String>> allShardingTables = new ConcurrentHashMap<>(64);

    public static Map<String,String> getTableShardingRule(){
        return tableShardingRule;
    }

    public static Map<String,LinkedList<String>> getAllShardingTable(){
        return allShardingTables;
    }

    public static String getTableShardingRuleByTableName(String tableName){
        return tableShardingRule.get(tableName);
    }

    public static LinkedList<String> getShardingTableByTableName(String tableName){
        return allShardingTables.get(tableName);
    }

    public static void setTableShardingRule(String tableName,String colum){
        tableShardingRule.put(tableName,colum);
    }

    public static void setAllShardingTables(String tableName,LinkedList<String> shardingTable){
        allShardingTables.put(tableName,shardingTable);
    }

}
