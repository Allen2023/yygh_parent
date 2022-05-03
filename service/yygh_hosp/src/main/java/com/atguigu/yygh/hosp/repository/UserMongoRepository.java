package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.hosp.bean.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Xu
 * @date 2022/4/18 20:31
 * yygh_parent com.atguigu.yygh.hosp.repository
 */

@Repository
public interface UserMongoRepository extends MongoRepository<User,String> {
    public List<User> findByNameLike(String name);
}
