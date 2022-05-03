package com.atguigu.yygh.user.service.impl;


import com.atguigu.enums.DictEnum;
import com.atguigu.model.user.Patient;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.user.mapper.PatientMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 就诊人表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-04-26
 */
@Service
public class PatientServiceImpl extends
        ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Autowired
    private DictFeignClient dictFeignClient;

    //获取就诊人列表
    @Override
    public List<Patient> findAllUserId(Long userId) {
        //根据userid查询所有就诊人信息列表
        QueryWrapper<Patient> wrapper = new QueryWrapper<>();
        List<Patient> patientList = baseMapper.selectList(new LambdaQueryWrapper<Patient>().eq(Patient::getUserId, userId));
        //通过远程调用，得到编码对应具体内容，查询数据字典表内容
        //其他参数封装
        patientList.forEach(this::packPatient);
        return patientList;
    }

    @Override
    public Patient getPatientId(Long id) {
        return this.packPatient(baseMapper.selectById(id));
    }

    //Patient对象里面其他参数封装
    private Patient packPatient(Patient patient) {
        //根据证件类型编码，获取证件类型具体指
        String certificatesTypeString = dictFeignClient.getNameByValue(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType());//联系人证件
        //联系人证件类型
        // String contactsCertificatesTypeString =dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(),patient.getContactsCertificatesType());
        //省
        String provinceString = dictFeignClient.getNameByValue(patient.getProvinceCode());
        //市
        String cityString = dictFeignClient.getNameByValue(patient.getCityCode());
        //区
        String districtString = dictFeignClient.getNameByValue(patient.getDistrictCode());
        patient.getParam().put("certificatesTypeString", certificatesTypeString);
        // patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
        patient.getParam().put("provinceString", provinceString);
        patient.getParam().put("cityString", cityString);
        patient.getParam().put("districtString", districtString);
        patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());
        return patient;
    }
}
