package com.atguigu.yygh.cmn.client;

/**
 * @author Xu
 * @date 2022/4/19 22:30
 * yygh_parent com.atguigu.yygh.cmn.client
 */

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Service
@FeignClient("yygh-cmn")
public interface DictFeignClient {

    /**
     * 根据value获取名称
     *
     * @param value
     * @return
     */
    @GetMapping(value = "/admin/cmn/getNameByValue/{value}")
    String getNameByValue(@PathVariable(value = "value") String value);

    /**
     * 根据dictCode和value获取名称
     *
     * @param dictCode
     * @param value
     * @return
     */
    @GetMapping(value = "/admin/cmn/getNameByValue/{dictCode}/{value}")
    String getNameByValue(@PathVariable(value = "dictCode") String dictCode, @PathVariable(value = "value") String value);
}
