package com.ruoyi.mental.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
                                                <script>
                                                SELECT COUNT(1)
                                                FROM mh_emotion_diary d
                                                WHERE d.del_flag = '0'
                                                        <if test='userId != null'>
                                                                AND d.user_id = #{userId}
                                                        </if>
                                                        <if test='minMoodScore != null'>
                                                                AND d.mood_score <![CDATA[ >= ]]> #{minMoodScore}
                                                        </if>
                                                        <if test='maxMoodScore != null'>
                                                                AND d.mood_score <![CDATA[ <= ]]> #{maxMoodScore}
                                                        </if>
                                                        <if test='dominantEmotion != null and dominantEmotion != ""'>
                                                                AND d.dominant_emotion = #{dominantEmotion}
                                                        </if>
                                                </script>
                                                """)
                long countDiaryForAdmin(@Param("userId") Long userId, @Param("minMoodScore") Integer minMoodScore,
                                                @Param("maxMoodScore") Integer maxMoodScore, @Param("dominantEmotion") String dominantEmotion);

                @Select("""
                                                <script>
                                                SELECT d.id,
                                                                         d.user_id AS userId,
                                                                         u.user_name AS userName,
                                                                         d.diary_date AS diaryDate,
                                                                         d.mood_score AS moodScore,
                                                                         d.dominant_emotion AS dominantEmotion,
                                                                         d.emotion_triggers AS emotionTriggers,
                                                                         d.diary_content AS diaryContent,
                                                                         d.sleep_quality AS sleepQuality,
                                                                         d.stress_level AS stressLevel,
                                                                         d.create_time AS createTime,
                                                                         d.update_time AS updateTime
                                                FROM mh_emotion_diary d
                                                LEFT JOIN sys_user u ON u.user_id = d.user_id
                                                WHERE d.del_flag = '0'
                                                        <if test='userId != null'>
                                                                AND d.user_id = #{userId}
                                                        </if>
                                                        <if test='minMoodScore != null'>
                                                                AND d.mood_score <![CDATA[ >= ]]> #{minMoodScore}
                                                        </if>
                                                        <if test='maxMoodScore != null'>
                                                                AND d.mood_score <![CDATA[ <= ]]> #{maxMoodScore}
                                                        </if>
                                                        <if test='dominantEmotion != null and dominantEmotion != ""'>
                                                                AND d.dominant_emotion = #{dominantEmotion}
                                                        </if>
                                                ORDER BY d.diary_date DESC, d.id DESC
                                                LIMIT #{offset}, #{size}
                                                </script>
                                                """)
                List<Map<String, Object>> selectDiaryForAdmin(@Param("userId") Long userId, @Param("minMoodScore") Integer minMoodScore,
                                                @Param("maxMoodScore") Integer maxMoodScore, @Param("dominantEmotion") String dominantEmotion,
                                                @Param("offset") int offset, @Param("size") int size);

                @Update("""
                                                UPDATE mh_emotion_diary
                                                SET del_flag = '2', update_time = NOW()
                                                WHERE id = #{id} AND del_flag = '0'
                                                """)
                int deleteDiaryById(@Param("id") Long id);

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
