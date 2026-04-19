package com.ruoyi.mental.domain.entity;

public class MentalChatSession
{
    private Long id;
    private Long userId;
    private String sessionTitle;
    private String lastMessage;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getSessionTitle()
    {
        return sessionTitle;
    }

    public void setSessionTitle(String sessionTitle)
    {
        this.sessionTitle = sessionTitle;
    }

    public String getLastMessage()
    {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage)
    {
        this.lastMessage = lastMessage;
    }
}
