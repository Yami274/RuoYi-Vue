package com.ruoyi.mental.mapper;

import java.time.LocalDate;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.ruoyi.mental.domain.entity.MentalEmotionDiary;

@Mapper
public interface MentalDiaryMapper
{
    @Select("""
            SELECT id
            FROM mh_emotion_diary
            WHERE user_id = #{userId} AND diary_date = #{diaryDate} AND del_flag = '0'
            """)
    Long selectDiaryId(@Param("userId") Long userId, @Param("diaryDate") LocalDate diaryDate);

    @Insert("""
            INSERT INTO mh_emotion_diary(user_id, diary_date, mood_score, dominant_emotion, emotion_triggers,
                                         diary_content, sleep_quality, stress_level, create_time, update_time, del_flag)
            VALUES(#{userId}, #{diaryDate}, #{moodScore}, #{dominantEmotion}, #{emotionTriggers},
                   #{diaryContent}, #{sleepQuality}, #{stressLevel}, NOW(), NOW(), '0')
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertDiary(MentalEmotionDiary diary);

    @Update("""
            UPDATE mh_emotion_diary
            SET mood_score = #{moodScore},
                dominant_emotion = #{dominantEmotion},
                emotion_triggers = #{emotionTriggers},
                diary_content = #{diaryContent},
                sleep_quality = #{sleepQuality},
                stress_level = #{stressLevel},
                update_time = NOW()
            WHERE id = #{id}
            """)
    int updateDiary(MentalEmotionDiary diary);
}
