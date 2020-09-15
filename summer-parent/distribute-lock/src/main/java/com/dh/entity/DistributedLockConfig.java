package com.dh.entity;

import java.util.List;
import java.util.Map;

/**
 * @author Created by linl on 2017/10/28.
 */
public class DistributedLockConfig {
    Integer lockMode;

    Long timeOut;

    boolean automaticRelease;

    Long tryLockTime;

    List<Map<String, Object>> lockKey;

    String key;

}
