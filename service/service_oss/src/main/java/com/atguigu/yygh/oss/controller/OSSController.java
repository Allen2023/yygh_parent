package com.atguigu.yygh.oss.controller;

import com.atguigu.yygh.oss.service.OSSService;
import com.atguigu.yygh.result.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Xu
 * @date 2022/4/25 22:57
 * yygh_parent com.atguigu.yygh.oos.controller
 */
@RestController
@RequestMapping("/admin/oss/file")
public class OSSController {
    @Autowired
    private OSSService ossService;

    @PostMapping("/upload")
    public R upload(MultipartFile file) throws IOException {
        String url = ossService.upload(file);
        return R.ok().data("url", url);
    }
}
