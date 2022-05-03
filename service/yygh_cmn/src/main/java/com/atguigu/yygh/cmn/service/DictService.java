package com.atguigu.yygh.cmn.service;


import com.atguigu.model.cmn.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2022-04-17
 */
public interface DictService extends IService<Dict> {
    //根据数据id查询子数据列表
    List<Dict> getChildListById(Long pid);

    //导出数据
    void exportData(HttpServletResponse response);

    //导入数据
    void importData(MultipartFile file) throws IOException;

    //根据dictCode和value查询name字段值
    String getNameByValue(String dictCode, String value);

    List<Dict> findByDictCode(String dictCode);

}
