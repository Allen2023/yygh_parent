package com.atguigu.yygh.cmn.service.impl;


import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.ExcelListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.model.cmn.Dict;
import com.atguigu.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-04-17
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    @Autowired
    private ExcelListener dictListener;



    //#pid和方法参数值要保持一致
    @Cacheable(value = "getChildListById", key = "'selectIndexList'+#pid")
    @Override
    public List<Dict> getChildListById(Long pid) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper();
        queryWrapper.eq("parent_id", pid);
        List<Dict> dictList = baseMapper.selectList(queryWrapper);
        for (Dict dict : dictList) {
            Long id = dict.getId();
            boolean flag = isHasChildList(id);
            dict.setHasChildren(flag);
        }
        return dictList;
    }

    //判断是否有子元素,大于0有,小于0则没有
    private boolean isHasChildList(Long id) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper();
        queryWrapper.eq("parent_id", id);
        Integer count = baseMapper.selectCount(queryWrapper);
        return count > 0;
    }

    //导出数据
    @Override
    public void exportData(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            List<Dict> dictList = baseMapper.selectList(null);
            List<DictEeVo> dictEeVoList = new ArrayList<>(dictList.size());
            //将属性值赋值给DictEeVo对象
            for (Dict dict : dictList) {
                DictEeVo dictEeVo = new DictEeVo();
                BeanUtils.copyProperties(dict, dictEeVo);
                dictEeVoList.add(dictEeVo);
            }
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典").doWrite(dictEeVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void importData(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), DictEeVo.class, dictListener).sheet(0).doRead();
    }

    @Override
    public String getNameByValue(String dictCode, String value) {
        if (StringUtils.isEmpty(dictCode)) {
            QueryWrapper<Dict> hospitalQueryWrapper = new QueryWrapper<>();
            hospitalQueryWrapper.eq("value", value);
            return baseMapper.selectOne(hospitalQueryWrapper).getName();
        } else {
            /*
             * select * from dict where
             * (select parent_id  from dict where dict_code ='Hostype') //dictCode
             * and value = 3; //value
             * */
            QueryWrapper<Dict> hospitalQueryWrapper1 = new QueryWrapper<>();
            hospitalQueryWrapper1.eq("dict_Code", dictCode);
            Long id = baseMapper.selectOne(hospitalQueryWrapper1).getId();
            //根据parent_id字段查询省市区name
            QueryWrapper<Dict> hospitalQueryWrapper2 = new QueryWrapper<>();
            //将id作为parent_id查询获得省市区name
            hospitalQueryWrapper2.eq("parent_id", id);
            hospitalQueryWrapper2.eq("value", value);
            return baseMapper.selectOne(hospitalQueryWrapper2).getName();
        }
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("dict_code", dictCode);
        Dict dict = baseMapper.selectOne(dictQueryWrapper);
        if (dict == null) {
            return null;
        } else {
            //查询子元素
            return this.findDataChildren(dict.getId());
        }
    }

    //查询子元素
    private List<Dict> findDataChildren(Long id) {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id", id);
        return baseMapper.selectList(dictQueryWrapper);
    }

}
