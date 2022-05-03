package com.atguigu.yygh.hosp.repository;

import com.atguigu.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Xu
 * @date 2022/4/19 1:29
 * yygh_parent com.atguigu.yygh.hosp.repository
 */
@Repository
public interface HospitalMongoRepository extends MongoRepository<Hospital,String> {


    Hospital findByHoscode(String hoscode);

    List<Hospital> findByHosnameLike(String hosname);

}
