package com.ruoyi.mental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mental.domain.req.SessionCreateReq;
import com.ruoyi.mental.domain.req.StreamChatReq;
import com.ruoyi.mental.service.MentalAppService;

@RestController
public class MentalChatController
{
    @Autowired
    private MentalAppService mentalAppService;

    @GetMapping("/psychological-chat/sessions")
    public AjaxResult pageSessions(
            @RequestParam(required = false) String emotionTag,
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize)
    {
        Long userId = SecurityUtils.getUserId();
        int finalPage = pageNum == null ? currentPage : pageNum;
        int finalSize = pageSize == null ? size : pageSize;
        AjaxResult ajax = AjaxResult.success();
        if (SecurityUtils.isAdmin(userId))
        {
            ajax.put("data", mentalAppService.pageSessionsForAdmin(emotionTag, finalPage, finalSize));
        }
        else
        {
            ajax.put("data", mentalAppService.pageSessions(userId, finalPage, finalSize));
        }
        return ajax;
    }

    @GetMapping("/psychological-chat/sessions/{sessionId}/messages")
    public AjaxResult listMessages(@PathVariable Long sessionId)
    {
        Long userId = SecurityUtils.getUserId();
        AjaxResult ajax = AjaxResult.success();
        if (SecurityUtils.isAdmin(userId))
        {
            ajax.put("data", mentalAppService.listMessagesForAdmin(sessionId));
        }
        else
        {
            ajax.put("data", mentalAppService.listMessages(userId, sessionId));
        }
        return ajax;
    }

    @PostMapping("/psychological-chat/session/start")
    public AjaxResult createSession(@RequestBody SessionCreateReq req)
    {
        Long userId = SecurityUtils.getUserId();
        AjaxResult ajax = AjaxResult.success("创建成功");
        ajax.put("data", mentalAppService.createSession(userId, req));
        return ajax;
    }

    @PostMapping("/psychological-chat/stream")
    public SseEmitter streamChat(@RequestBody StreamChatReq req)
    {
        Long userId = SecurityUtils.getUserId();
        return mentalAppService.streamChat(userId, req);
    }

    @GetMapping("/psychological-chat/session/{sessionId}/emotion")
    public AjaxResult getEmotion(@PathVariable Long sessionId)
    {
        Long userId = SecurityUtils.getUserId();
        AjaxResult ajax = AjaxResult.success();
        ajax.put("data", mentalAppService.getSessionEmotion(userId, sessionId));
        return ajax;
    }

    @DeleteMapping("/psychological-chat/sessions/{sessionId}")
    public AjaxResult deleteSession(@PathVariable Long sessionId)
    {
        Long userId = SecurityUtils.getUserId();
        mentalAppService.deleteSession(userId, sessionId);
        return AjaxResult.success("删除成功");
    }
}
