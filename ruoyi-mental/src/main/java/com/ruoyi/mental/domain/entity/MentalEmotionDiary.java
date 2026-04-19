package com.ruoyi.mental.domain.entity;

import java.time.LocalDate;

public class MentalEmotionDiary
{
    private Long id;
    private Long userId;
    private LocalDate diaryDate;
    private Integer moodScore;
    private String dominantEmotion;
    private String emotionTriggers;
    private String diaryContent;
    private Integer sleepQuality;
    private Integer stressLevel;

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

    public LocalDate getDiaryDate()
    {
        return diaryDate;
    }

    public void setDiaryDate(LocalDate diaryDate)
    {
        this.diaryDate = diaryDate;
    }

    public Integer getMoodScore()
    {
        return moodScore;
    }

    public void setMoodScore(Integer moodScore)
    {
        this.moodScore = moodScore;
    }

    public String getDominantEmotion()
    {
        return dominantEmotion;
    }

    public void setDominantEmotion(String dominantEmotion)
    {
        this.dominantEmotion = dominantEmotion;
    }

    public String getEmotionTriggers()
    {
        return emotionTriggers;
    }

    public void setEmotionTriggers(String emotionTriggers)
    {
        this.emotionTriggers = emotionTriggers;
    }

    public String getDiaryContent()
    {
        return diaryContent;
    }

    public void setDiaryContent(String diaryContent)
    {
        this.diaryContent = diaryContent;
    }

    public Integer getSleepQuality()
    {
        return sleepQuality;
    }

    public void setSleepQuality(Integer sleepQuality)
    {
        this.sleepQuality = sleepQuality;
    }

    public Integer getStressLevel()
    {
        return stressLevel;
    }

    public void setStressLevel(Integer stressLevel)
    {
        this.stressLevel = stressLevel;
    }
}
