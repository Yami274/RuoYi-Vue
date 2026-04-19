package com.ruoyi.mental.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mental.domain.req.AdminArticleReq;
import com.ruoyi.mental.domain.req.AdminArticleStatusReq;
import com.ruoyi.mental.service.MentalAppService;

@RestController
public class MentalKnowledgeController
{
    @Autowired
    private MentalAppService mentalAppService;

    @GetMapping("/knowledge/article/page")
    public AjaxResult pageArticles(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String authorName,
            @RequestParam(defaultValue = "readCount") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "10") int size)
    {
        Long userId = SecurityUtils.getUserId();
        AjaxResult ajax = AjaxResult.success();
        if (SecurityUtils.isAdmin(userId))
        {
            ajax.put("data", mentalAppService.pageKnowledgeForAdmin(title, categoryId, status, authorName, sortField, sortDirection, currentPage, size));
        }
        else
        {
            ajax.put("data", mentalAppService.pageKnowledge(sortField, sortDirection, currentPage, size));
        }
        return ajax;
    }

    @GetMapping("/knowledge/category/tree")
    public AjaxResult categoryTree()
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("data", mentalAppService.categoryTreeForAdmin());
        return ajax;
    }

    @PostMapping("/knowledge/article")
    public AjaxResult addArticle(@RequestBody AdminArticleReq req)
    {
        mentalAppService.createArticleForAdmin(req);
        return AjaxResult.success("新增成功");
    }

    @GetMapping("/knowledge/article/{id}")
    public AjaxResult articleDetail(@PathVariable Long id)
    {
        Long userId = SecurityUtils.getUserId();
        AjaxResult ajax = AjaxResult.success();
        if (SecurityUtils.isAdmin(userId))
        {
            ajax.put("data", mentalAppService.getKnowledgeDetailForAdmin(id));
        }
        else
        {
            ajax.put("data", mentalAppService.getKnowledgeDetail(id));
        }
        return ajax;
    }

    @PutMapping("/knowledge/article/{id}")
    public AjaxResult updateArticle(@PathVariable Long id, @RequestBody AdminArticleReq req)
    {
        mentalAppService.updateArticleForAdmin(id, req);
        return AjaxResult.success("更新成功");
    }

    @PutMapping("/knowledge/article/{id}/status")
    public AjaxResult updateStatus(@PathVariable Long id, @RequestBody AdminArticleStatusReq req)
    {
        mentalAppService.updateArticleStatusForAdmin(id, req);
        return AjaxResult.success("状态更新成功");
    }

    @DeleteMapping("/knowledge/article/{id}")
    public AjaxResult deleteArticle(@PathVariable Long id)
    {
        mentalAppService.deleteArticleForAdmin(id);
        return AjaxResult.success("删除成功");
    }
}
