package com.ruoyi.mental.domain.req;

public record AdminArticleReq(String title, String content, String coverImage, Integer categoryId, String summary, String tags, String id)
{
}
