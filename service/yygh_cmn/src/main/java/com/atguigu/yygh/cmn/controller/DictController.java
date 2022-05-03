package com.atguigu.yygh.cmn.controller;


import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.model.cmn.Dict;
import com.atguigu.yygh.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * <p>
 * 组织架构表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-04-17
 */
@Api(description = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn")
public class DictController {

    @Autowired
    private DictService dictService;
    //根据数据id查询子数据列表


    @ApiOperation(value = "根据数据id查询子数据列表")
    @GetMapping("/childList/{pid}")
    public R childList(@PathVariable Long pid) {
        List<Dict> dictList = dictService.getChildListById(pid);
        return R.ok().data("list", dictList);
    }

    @ApiOperation(value = "导出")
    @GetMapping("/exportData")
    public void exportData(HttpServletResponse response) {
        dictService.exportData(response);
    }

    @ApiOperation(value = "导入")
    @PostMapping("/importData")
    public R importData(MultipartFile file) throws IOException {
        dictService.importData(file);
        return R.ok();
    }

    @ApiOperation(value = "远程调用查询省市区信息")
    @GetMapping("/getNameByValue/{dictCode}/{value}")
    public String getNameByValue(@PathVariable(value = "dictCode") String dictCode, @PathVariable(value = "value") String value) {
        return dictService.getNameByValue(dictCode, value);
    }

    @ApiOperation(value = "远程调用查询省市区信息")
    @GetMapping("/getNameByValue/{value}")
    public String getNameByValue(@PathVariable(value = "value") String value) {
        return dictService.getNameByValue("", value);
    }

    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping("/findByDictCode/{dictCode}")
    public R findByDictCode(@PathVariable(value = "dictCode") String dictCode) {
        List<Dict> list = dictService.findByDictCode(dictCode);
        return R.ok().data("list", list);
    }



}
