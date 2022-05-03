package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.model.hosp.BookingRule;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.enums.DictEnum;
import com.atguigu.yygh.hosp.repository.HospitalMongoRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.model.hosp.Hospital;
import com.atguigu.vo.hosp.HospitalQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;


import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/19 1:31
 * yygh_parent com.atguigu.yygh.hosp.service.impl
 */
@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalMongoRepository hospitalMongoRepository;

    @Autowired
    private DictFeignClient dictFeignClient;

    private Hospital hosp;

    @Override
    public Hospital getByHoscode(String hoscode) {

        return hospitalMongoRepository.findByHoscode(hoscode);
    }

    @Override
    public void save(Map<String, Object> resultMap) {
        Hospital hospital = JSONObject.parseObject(JSONObject.toJSONString(resultMap), Hospital.class);

        Hospital queryHospital = hospitalMongoRepository.findByHoscode(hospital.getHoscode());
        if (queryHospital == null) {
            //不存在此数据则添加
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalMongoRepository.save(hospital);
        } else {
            //存在则修改数据
            hospital.setId(queryHospital.getId());
            hospital.setStatus(queryHospital.getStatus());
            hospital.setCreateTime(queryHospital.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(queryHospital.getIsDeleted());
            hospitalMongoRepository.save(hospital);
        }
    }

    @Override
    public Page<Hospital> getHospitalPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Hospital hospital = new Hospital();
        hospital.setHosname(hospitalQueryVo.getHosname());
        hospital.setHostype(hospitalQueryVo.getHostype());
        hospital.setProvinceCode(hospitalQueryVo.getProvinceCode());
        hospital.setCityCode(hospitalQueryVo.getCityCode());
        hospital.setDistrictCode(hospitalQueryVo.getDistrictCode());
        //创建匹配器,既如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("hosname", ExampleMatcher.GenericPropertyMatchers.contains())
                .withIgnoreCase(true);

        Example<Hospital> hospitalExample = Example.of(hospital, matcher);
        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.ASC, "createTime"));

        Page<Hospital> pageResult = hospitalMongoRepository.findAll(hospitalExample, pageRequest);
        //用openFeign远程调用数据字典 遍历给Hospital属性赋值 封装数据
        pageResult.getContent().parallelStream().forEach(this::packHospital);
        return pageResult;
    }

    //封装数据
    private void packHospital(Hospital hosp) {
        this.hosp = hosp;
        String hostype = dictFeignClient.getNameByValue(DictEnum.HOSTYPE.getDictCode(), hosp.getHostype());
        String provinceCode = dictFeignClient.getNameByValue(hosp.getProvinceCode());
        String cityCode = dictFeignClient.getNameByValue(hosp.getCityCode());
        String districtCode = dictFeignClient.getNameByValue(hosp.getDistrictCode());
        //将医院等级和医院地址放入map返回
        hosp.getParam().put("hostype", hostype);
        hosp.getParam().put("fullAddress", provinceCode + cityCode + districtCode + hosp.getAddress());
    }

    //更新上线状态
    @Override
    public void lock(String id, Integer status) {
        if (status == 0 || status == 1) {
            Hospital hospital = hospitalMongoRepository.findById(id).get();
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospitalMongoRepository.save(hospital);
        }
    }

    @Override
    public Map<String, Object> show(String id) {
        Map<String, Object> result = new HashMap<>();
        Hospital hospital = hospitalMongoRepository.findById(id).get();
        this.packHospital(hospital);
        result.put("hospital", hospital);
        result.put("bookingRule", hospital.getBookingRule());
        return result;
    }

    @Override
    public List<Hospital> findByHosname(String hosname) {
        return hospitalMongoRepository.findByHosnameLike(hosname);
    }

    @Override
    public Map<String, Object> findByHoscode(String hoscode) {
        Map<String, Object> result = new HashMap<>();
        Hospital hospital = hospitalMongoRepository.findByHoscode(hoscode);
        //将数字替换成中文远程调用数据字典
        this.packHospital(hospital);
        BookingRule bookingRule = hospital.getBookingRule();
        result.put("hospital", hospital);
        result.put("bookingRule", bookingRule);
        return result;


    }
}
