package com.ruoyi.mental.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mental.domain.req.UserLoginReq;
import com.ruoyi.mental.domain.req.UserRegisterReq;
import com.ruoyi.mental.service.MentalAppService;

@RestController
public class MentalUserController
{
    @Autowired
    private MentalAppService mentalAppService;

    @PostMapping("/user/login")
    public AjaxResult login(@RequestBody UserLoginReq req)
    {
        Map<String, Object> data = mentalAppService.login(req);
        AjaxResult ajax = AjaxResult.success("登录成功");
        ajax.put("data", data);
        return ajax;
    }

    @PostMapping("/user/add")
    public AjaxResult register(@RequestBody UserRegisterReq req)
    {
        mentalAppService.register(req);
        return AjaxResult.success("注册成功");
    }

    @PostMapping("/user/logout")
    public AjaxResult logout()
    {
        Map<String, Object> data = new HashMap<>();
        data.put("logout", true);
        AjaxResult ajax = AjaxResult.success("退出成功");
        ajax.put("data", data);
        return ajax;
    }
}
