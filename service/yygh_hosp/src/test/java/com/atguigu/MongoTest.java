/*
package com.atguigu;

import com.atguigu.yygh.hosp.ServiceHospApplication;
import com.atguigu.yygh.hosp.bean.User;

import com.atguigu.yygh.hosp.repository.UserMongoRepository;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;


import java.util.*;
import java.util.regex.Pattern;

*/
/**
 * @author Xu
 * @date 2022/4/18 18:38
 * yygh_parent com.atguigu
 *//*

//@SpringBootTest(classes = ServiceHospApplication.class)
public class MongoTest {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserMongoRepository userMongoRepository;

    //添加
    @Test
    public void testInsert() {
        User user = mongoTemplate.findById("2", User.class);
        user.setAge(30);
        mongoTemplate.save(user);
        //mongoTemplate.insert(new User("1","小徐" , 18, "1273343014@qq.com", new Date().toString()));
        //mongoTemplate.insert(new User("8", "小马六", 19, "1273343014@qq.com", new Date().toString()));
        //mongoTemplate.insert(new User("9", "小六八", 20, "1273343014@qq.com", new Date().toString()));
        //mongoTemplate.insert(new User("10", "小七八", 21, "1273343014@qq.com", new Date().toString()));

    }

    //删除
    @Test
    public void testDelete() {
        Query query = new Query();
        //设置删除条件
        //query.addCriteria(Criteria.where("_id").is("10"));
        //多个条件同时满足才删除
        //query.addCriteria(Criteria.where("age").is(30).and("username").is("小马"));
        //满足其中一个条件删除
        Criteria criteria = new Criteria();
        query.addCriteria(criteria.orOperator(Criteria.where("age").is(30), Criteria.where("username").is("小马")));
        DeleteResult result = mongoTemplate.remove(query, User.class);
        //删除成功的个数
        System.out.println(result.getDeletedCount());

    }

    //更新
    //save(),先查再改
    //updateFirst()
    //updateMulti()
    @Test
    public void update() {
        Query query = new Query(Criteria.where("_id").is("1"));
        Update update = new Update();
        update.set("username", "小贾");
        UpdateResult result = mongoTemplate.updateFirst(query, update, User.class);
        System.out.println("result = " + result);
    }

    //查询
    @Test
    public void findUser() {
        //查所有
        //List<User> userList = mongoTemplate.findAll(User.class);
        //根据id查询
        //User user = mongoTemplate.findById("1", User.class);
        //条件查询
        */
/*Query query = new Query(Criteria.where("name").is("小徐").and("age").is(18));
        List<User> userList = mongoTemplate.find(query, User.class);*//*

        //模糊查询
        Pattern pattern = Pattern.compile(String.format("%s%s%s", "^.*", "马", ".*$"), Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("name").regex(pattern));
        List<User> userList = mongoTemplate.find(query, User.class);
        userList.forEach(System.out::println);
        System.out.println(String.format("%s%s%s", "^.*", "马", ".*$"));
    }

    //分页查询
    @Test
    public void findUsersPage() {
        int current = 1;
        int PageSize = 3;
        Pattern pattern = Pattern.compile(String.format("%s%s%s", "^.*", "小", ".*$"), Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("name").regex(pattern));
        //先查询总数据个数
        int totalCount = (int) mongoTemplate.count(query, User.class);
        List<User> userList = mongoTemplate.find(query.skip((current - 1) * PageSize).limit(PageSize), User.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("list", userList);
        map.put("totalCount", totalCount);
        //System.out.println(map);
        userList.forEach(System.out::println);
    }


    //MongoRepository

    */
/**
     * 添加操作
     *//*

    @Test
    public void testInsert2() {
        //User user = userMongoRepository.insert(new User("10", "小徐", 18, "1273343014@qq.com", new Date().toString()));
        //User user = userMongoRepository.save(new User("12", "小徐", 18, "1273343014@qq.com", new Date().toString()));
        User user1 = new User("13", "小徐", 18, "1273343014@qq.com", new Date().toString());
        User user2 = new User("14", "小徐", 18, "1273343014@qq.com", new Date().toString());
        User user3 = new User("15", "小徐", 18, "1273343014@qq.com", new Date().toString());
        User user4 = new User("16", "小徐", 18, "1273343014@qq.com", new Date().toString());
        List<User> list = new ArrayList<>();
        list.add(user1);
        list.add(user2);
        list.add(user3);
        list.add(user4);
        List<User> userList = userMongoRepository.saveAll(list);
        userList.forEach(System.out::println);
    }

    */
/**
     * 删除操作
     *//*

    @Test
    public void testDelete2() {
        //userMongoRepository.deleteById("16");
        User user = new User();
        user.setId("13");
        userMongoRepository.delete(user);//对象中的id删除
    }

    */
/**
     * 修改操作
     *//*

    //@Test
    public void testUpdate2() {

//        userMongoRepository.save();
    }

    */
/**
     * 查询操作
     *//*

    @Test
    public void testSelect2() {
        //User user = userMongoRepository.findById("1").get();

        //设置对象实例 对象中的属性作为and条件查询
       */
/* Example<User> example = Example.of(new User(null, "小徐", null, null, null));
        User user = userMongoRepository.findOne(example).get();*//*

        User user = new User(null, "六", null, null, null);
        ExampleMatcher matcher = ExampleMatcher.matching()
                //.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                //.withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains())
                //.withMatcher("name", ExampleMatcher.GenericPropertyMatchers.startsWith())
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.endsWith())
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写
        Example<User> example = Example.of(user, matcher);
        List<User> userList = userMongoRepository.findAll(example);
        userList.forEach(System.out::println);

    }

    //分页查询
    @Test
    public void testPage() {
        //MongoRepository中page是第一页
        int page = 0;
        int size = 3;
        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写
        User user = new User();
        user.setName("小徐");
        //创建example实例
        Example<User> userExample = Example.of(user, matcher);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<User> userPage = userMongoRepository.findAll(userExample, pageable);
        userPage.forEach(System.out::println);
        List<User> userList = userPage.getContent();
        System.out.println("============================");
        userList.forEach(System.out::println);
        System.out.println(" 总页数= " + userPage.getTotalPages());
        System.out.println("总记录数= " + userPage.getTotalElements());

    }

    @Test
    public void testFind(){
        List<User> userList = userMongoRepository.findByNameLike("徐");
        for (User user : userList) {
            System.out.println("user = " + user);
        }
    }
}
*/
