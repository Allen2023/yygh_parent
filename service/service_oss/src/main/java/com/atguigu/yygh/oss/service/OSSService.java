package com.atguigu.yygh.oss.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;

import com.atguigu.yygh.oss.prop.ConstantPropertiesUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;


/**
 * @author Xu
 * @date 2022/4/25 22:57
 * yygh_parent com.atguigu.yygh.oos.service
 */
@Service
public class OSSService {


    public String upload(MultipartFile file) throws IOException {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = ConstantPropertiesUtil.END_POINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ConstantPropertiesUtil.ACCESS_KEY_ID;
        String accessKeySecret = ConstantPropertiesUtil.ACCESS_KEY_SECRET;
        // 填写Bucket名称，例如examplebucket。
        String bucketName = ConstantPropertiesUtil.BUCKET_NAME;


        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            //String originalFilename = file.getOriginalFilename();
            String fileName = new DateTime().toString("yyyy/MM/dd")+"/"+ UUID.randomUUID().toString().replaceAll("-", "") + file.getOriginalFilename();
            // 创建PutObject请求。
            ossClient.putObject(bucketName, fileName, file.getInputStream());
            String url = "https://" + bucketName + "." + endpoint + "/" + fileName;
            return url;
        } catch (OSSException oe) {
            oe.printStackTrace();
            return null;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

    }

}
