package com.ruoyi.mental.domain.req;

public record EmotionDiaryReq(
        String diaryDate,
        Integer moodScore,
        String dominantEmotion,
        String emotionTriggers,
        String diaryContent,
        Integer sleepQuality,
        Integer stressLevel)
{
}
