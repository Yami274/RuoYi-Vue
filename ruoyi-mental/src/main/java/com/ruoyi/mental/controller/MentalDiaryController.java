package com.ruoyi.mental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mental.domain.req.EmotionDiaryReq;
import com.ruoyi.mental.service.MentalAppService;

@RestController
public class MentalDiaryController
{
    @Autowired
    private MentalAppService mentalAppService;

    @PostMapping("/emotion-diary")
    public AjaxResult upsertEmotionDiary(@RequestBody EmotionDiaryReq req)
    {
        Long userId = SecurityUtils.getUserId();
        mentalAppService.upsertDiary(userId, req);
        return AjaxResult.success("保存成功");
    }
}
