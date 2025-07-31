package com.sky.controller.admin;


import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
// 加入ioc容器
@RestController
// 设置请求路径
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    // 配置类已经生成对应的bean
    @Autowired()
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("upload")
    @ApiOperation("文件上传")
    // 接收二进制文件，和前端参数一致
    public Result<String> upload(MultipartFile file) {

        log.info("文件上传：{}", file);

        try {
            
            // 原始文件名
            String originalFilename = file.getOriginalFilename();
            // 截取原始文件名的后缀 xxx.jpg
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 构建新文件名称
            String objectName = UUID.randomUUID().toString() + extension;

            // 第一个参数为图片本身，第二个为图片名称
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);

            return Result.success(filePath);

        } catch (IOException e) {
            log.info("文件上传失败：{}", e);
        }
        // 提示文件上传失败
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
//https://web-framework-xin.oss-cn-hangzhou.aliyuncs.com/ec8737fe-4c89-47d5-bb7b-c2fe97af054d.jpg