package com.atguigu.yygh.hosp.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/19 1:14
 * yygh_parent com.atguigu.yygh.hosp.utils
 */

public class HttpRequestHelper {
    public static Map<String, Object> switchMap(Map<String, String[]> parameterMap) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        //遍历获取键值对放入resultMap并返回
        parameterMap.forEach((key, value1) -> {
            String value = value1[0];
            resultMap.put(key, value);
        });
        return resultMap;
    }
}
