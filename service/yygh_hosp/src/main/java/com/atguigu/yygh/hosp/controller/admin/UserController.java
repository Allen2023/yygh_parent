package com.atguigu.yygh.hosp.controller.admin;

import com.atguigu.yygh.result.R;
import com.atguigu.vo.acl.UserQueryVo;
import org.springframework.web.bind.annotation.*;

/**
 * @author Xu
 * @date 2022/4/15 19:51
 * yygh_parent com.atguigu.yygh.hosp.controller
 */
@RestController
@RequestMapping("/admin/user")
public class UserController {

    //用户登录
    @PostMapping("/login")
    public R login(@RequestBody UserQueryVo userQueryVo){

        return R.ok().data("token","admin-token");
    }

    @GetMapping("/info")
    public R info(String token){
        System.out.println(token);
        return R.ok()
                .data("roles","[admin]")
                .data("introduction","I am a super administrator")
                .data("avatar","https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg1.doubanio.com%2Fview%2Fphoto%2Fl%2Fpublic%2Fp2522197538.jpg&refer=http%3A%2F%2Fimg1.doubanio.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1652617427&t=75427c59bd28f0cf346978687731d531")
                .data("name","Super Admin");
    }


    @PostMapping("/logout")
    public R logout(){
        return R.ok();
    }
}
