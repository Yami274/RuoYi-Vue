package com.ruoyi.mental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/emotion-diary/admin/page")
    public AjaxResult pageDiaryForAdmin(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer minMoodScore,
            @RequestParam(required = false) Integer maxMoodScore,
            @RequestParam(required = false) String dominantEmotion,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size)
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("data", mentalAppService.pageDiaryForAdmin(userId, minMoodScore, maxMoodScore, dominantEmotion, current, size));
        return ajax;
    }

    @DeleteMapping("/emotion-diary/admin/{id}")
    public AjaxResult deleteDiaryForAdmin(@PathVariable Long id)
    {
        mentalAppService.deleteDiaryForAdmin(id);
        return AjaxResult.success("删除成功");
    }
}
