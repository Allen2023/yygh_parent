package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.vo.hosp.DepartmentVo;
import com.atguigu.yygh.exception.YYGHException;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.model.hosp.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Xu
 * @date 2022/4/19 18:35
 * yygh_parent com.atguigu.yygh.hosp.service.impl
 */

@Service
public class DepartmentServiceImpl implements DepartmentService {


    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void save(Map<String, Object> stringObjectMap) {
        Department department = JSONObject.parseObject(JSONObject.toJSONString(stringObjectMap), Department.class);
        String hoscode = department.getHoscode();
        String depcode = department.getDepcode();

        Department mongoDepartment = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);

        if (mongoDepartment == null) {
            department.setCreateTime(new Date());
            department.setCreateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        } else {
            department.setCreateTime(department.getCreateTime());
            department.setCreateTime(new Date());
            department.setIsDeleted(department.getIsDeleted());
            department.setId(department.getId());
            departmentRepository.save(department);
        }
    }

    @Override
    public Page<Department> getDepartmentList(Map<String, Object> stringObjectMap) {
        //????????????map????????????
        String hoscode = (String) stringObjectMap.get("hoscode");
        String page = (String) stringObjectMap.get("page");
        String limit = (String) stringObjectMap.get("limit");
        Department department = new Department();
        //???????????????????????????????????????
        department.setHoscode(hoscode);
        department.setIsDeleted(0);
        Example<Department> departmentExample = Example.of(department);
        PageRequest pageRequest = PageRequest.of(Integer.parseInt(page) - 1, Integer.parseInt(limit), Sort.by(Sort.Direction.ASC, "createTime"));
        return departmentRepository.findAll(departmentExample, pageRequest);
    }

    @Override
    public void removeDepartment(Map<String, Object> stringObjectMap) {
        //????????????map????????????
        String hoscode = (String) stringObjectMap.get("hoscode");
        String depcode = (String) stringObjectMap.get("depcode");
        Department department = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);
        if (department == null) {
            throw new YYGHException(20001, "??????????????????");
        }
        department.setIsDeleted(1);
        departmentRepository.save(department);
    }

    @Override
    public List<DepartmentVo> getDeptList(String hoscode) {
        //?????????
        List<DepartmentVo> treeList = new ArrayList<DepartmentVo>();

        Department departmentQuery = new Department();
        departmentQuery.setHoscode(hoscode);
        Example example = Example.of(departmentQuery);
        //???????????????????????????????????????
        List<Department> departmentlist = departmentRepository.findAll(example);
        //????????????????????????????????????????????????map
        Map<String, List<Department>> deparmentMap =
                departmentlist.stream().collect(Collectors.groupingBy(Department::getBigcode));
        //??????map????????????
        for (Map.Entry<String, List<Department>> entry : deparmentMap.entrySet()) {
            //???????????????
            String bigcode = entry.getKey();
            //?????????????????????????????????????????????
            List<Department> smallDepartmentList = entry.getValue();
            //???????????????
            DepartmentVo departmentVo1 = new DepartmentVo();
            departmentVo1.setDepcode(bigcode);
            departmentVo1.setDepname(smallDepartmentList.get(0).getBigname());
            //???????????????
            List<DepartmentVo> children = new ArrayList<>();
            for (Department department : smallDepartmentList) {
                DepartmentVo departmentVo2 = new DepartmentVo();
                departmentVo2.setDepname(department.getDepname());
                departmentVo2.setDepcode(department.getDepcode());
                children.add(departmentVo2);
            }
            //???????????????????????????????????????????????????
            departmentVo1.setChildren(children);
            //?????????????????????????????????
            treeList.add(departmentVo1);
        }
        return treeList;
    }

    @Override
    public Department findByHoscodeAndDepcode(String hoscode, String depcode) {

        return  departmentRepository.findByHoscodeAndDepcode(hoscode,depcode);
    }
}
