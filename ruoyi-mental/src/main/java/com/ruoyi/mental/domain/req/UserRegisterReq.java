package com.ruoyi.mental.domain.req;

public record UserRegisterReq(
        String username,
        String email,
        String nickname,
        String phone,
        String password,
        String confirmPassword,
        Integer gender,
        Integer userType)
{
}
