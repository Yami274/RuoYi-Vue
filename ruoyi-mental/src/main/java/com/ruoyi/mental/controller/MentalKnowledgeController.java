package com.ruoyi.mental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mental.service.MentalAppService;

@RestController
public class MentalKnowledgeController
{
    @Autowired
    private MentalAppService mentalAppService;

    @GetMapping("/knowledge/article/page")
    public AjaxResult pageArticles(
            @RequestParam(defaultValue = "readCount") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "10") int size)
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("data", mentalAppService.pageKnowledge(sortField, sortDirection, currentPage, size));
        return ajax;
    }

    @GetMapping("/knowledge/article/{id}")
    public AjaxResult articleDetail(@PathVariable Long id)
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("data", mentalAppService.getKnowledgeDetail(id));
        return ajax;
    }
}
