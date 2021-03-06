package com.atguigu.yygh.hosp.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("User")
public class User {
    @Id
    private String id;
    @Field(value = "username")
    private String name;
    private Integer age;
    private String email;
    private String createDate;
}