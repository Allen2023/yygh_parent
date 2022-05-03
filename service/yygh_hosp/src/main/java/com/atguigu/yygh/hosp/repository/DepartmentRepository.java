package com.atguigu.yygh.hosp.repository;

import com.atguigu.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Xu
 * @date 2022/4/19 18:41
 * yygh_parent com.atguigu.yygh.hosp.repository
 */
@Repository
public interface DepartmentRepository extends MongoRepository<Department,String> {
    Department findByHoscodeAndDepcode(String hoscode, String depcode);
}
