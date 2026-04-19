package com.ruoyi.mental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.mental.service.MentalAppService;

@RestController
public class MentalFileController
{
    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private MentalAppService mentalAppService;

    @PostMapping("/file/upload")
    public AjaxResult uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam("businessType") String businessType,
            @RequestParam("businessId") String businessId,
            @RequestParam("businessField") String businessField)
    {
        try
        {
            String filePath = RuoYiConfig.getUploadPath();
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;

            mentalAppService.saveUploadRecord(businessType, businessId, businessField, file.getOriginalFilename(),
                    FileUtils.getName(fileName), url, file.getSize());

            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", fileName);
            ajax.put("newFileName", FileUtils.getName(fileName));
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }
}
