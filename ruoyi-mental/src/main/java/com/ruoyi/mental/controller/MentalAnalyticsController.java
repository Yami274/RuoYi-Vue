package com.ruoyi.mental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mental.service.MentalAppService;

@RestController
public class MentalAnalyticsController
{
    @Autowired
    private MentalAppService mentalAppService;

    @GetMapping("/data-analytics/overview")
    public AjaxResult overview()
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("data", mentalAppService.getOverviewAnalytics());
        return ajax;
    }
}
