package com.ruoyi.mental.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.ruoyi.mental.domain.entity.MentalChatMessage;
import com.ruoyi.mental.domain.entity.MentalChatSession;

@Mapper
public interface MentalChatMapper
{
                @Select("""
                                                <script>
                                                SELECT s.id,
                                                                         s.user_id AS userId,
                                                                         u.user_name AS userName,
                                                                         s.session_title AS sessionTitle,
                                                                         s.last_message AS lastMessage,
                                                                         IFNULL(e.dominant_emotion, '') AS dominantEmotion,
                                                                         IFNULL(e.mood_score, 0) AS moodScore,
                                                                         s.create_time AS createTime,
                                                                         s.update_time AS updateTime
                                                FROM mh_chat_session s
                                                LEFT JOIN mh_chat_emotion e ON e.session_id = s.id AND e.user_id = s.user_id
                                                LEFT JOIN sys_user u ON u.user_id = s.user_id
                                                WHERE s.del_flag = '0'
                                                        <if test='emotionTag != null and emotionTag != ""'>
                                                                AND e.dominant_emotion = #{emotionTag}
                                                        </if>
                                                ORDER BY s.update_time DESC
                                                LIMIT #{offset}, #{size}
                                                </script>
                                                """)
                List<Map<String, Object>> selectSessionsForAdmin(@Param("emotionTag") String emotionTag, @Param("offset") int offset,
                                                @Param("size") int size);

                @Select("""
                                                <script>
                                                SELECT COUNT(1)
                                                FROM mh_chat_session s
                                                LEFT JOIN mh_chat_emotion e ON e.session_id = s.id AND e.user_id = s.user_id
                                                WHERE s.del_flag = '0'
                                                        <if test='emotionTag != null and emotionTag != ""'>
                                                                AND e.dominant_emotion = #{emotionTag}
                                                        </if>
                                                </script>
                                                """)
                long countSessionsForAdmin(@Param("emotionTag") String emotionTag);

                @Select("""
                                                SELECT m.id,
                                                                         m.role,
                                                                         m.content,
                                                                         m.user_id AS userId,
                                                                         m.create_time AS createTime
                                                FROM mh_chat_message m
                                                JOIN mh_chat_session s ON s.id = m.session_id
                                                WHERE m.session_id = #{sessionId}
                                                        AND m.del_flag = '0'
                                                        AND s.del_flag = '0'
                                                ORDER BY m.id ASC
                                                """)
                List<Map<String, Object>> selectMessagesForAdmin(@Param("sessionId") Long sessionId);

    @Select("""
            SELECT id,
                   session_title AS sessionTitle,
                   last_message AS lastMessage,
                   create_time AS createTime,
                   update_time AS updateTime
            FROM mh_chat_session
            WHERE user_id = #{userId} AND del_flag = '0'
            ORDER BY update_time DESC
            LIMIT #{offset}, #{size}
            """)
    List<Map<String, Object>> selectSessions(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    @Select("""
            SELECT COUNT(1)
            FROM mh_chat_session
            WHERE user_id = #{userId} AND del_flag = '0'
            """)
    long countSessions(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO mh_chat_session(user_id, session_title, last_message, create_time, update_time, del_flag)
            VALUES(#{userId}, #{sessionTitle}, #{lastMessage}, NOW(), NOW(), '0')
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSession(MentalChatSession session);

    @Select("""
            SELECT COUNT(1)
            FROM mh_chat_session
            WHERE id = #{sessionId} AND user_id = #{userId} AND del_flag = '0'
            """)
    int countSession(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    @Update("""
            UPDATE mh_chat_session
            SET del_flag = '2', update_time = NOW()
            WHERE id = #{sessionId} AND user_id = #{userId}
            """)
    int deleteSession(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    @Update("""
            UPDATE mh_chat_message
            SET del_flag = '2'
            WHERE session_id = #{sessionId} AND user_id = #{userId}
            """)
    int deleteSessionMessages(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    @Select("""
            SELECT id,
                   role,
                   content,
                   create_time AS createTime
            FROM mh_chat_message
            WHERE session_id = #{sessionId} AND user_id = #{userId} AND del_flag = '0'
            ORDER BY id ASC
            """)
    List<Map<String, Object>> selectMessages(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    @Insert("""
            INSERT INTO mh_chat_message(session_id, user_id, role, content, create_time, del_flag)
            VALUES(#{sessionId}, #{userId}, #{role}, #{content}, NOW(), '0')
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMessage(MentalChatMessage message);

    @Update("""
            UPDATE mh_chat_session
            SET last_message = #{lastMessage}, update_time = NOW()
            WHERE id = #{sessionId} AND user_id = #{userId}
            """)
    int refreshSession(@Param("sessionId") Long sessionId, @Param("userId") Long userId, @Param("lastMessage") String lastMessage);

    @Select("""
            SELECT COUNT(1)
            FROM mh_chat_emotion
            WHERE session_id = #{sessionId} AND user_id = #{userId}
            """)
    int countEmotion(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    @Insert("""
            INSERT INTO mh_chat_emotion(session_id, user_id, dominant_emotion, mood_score, update_time)
            VALUES(#{sessionId}, #{userId}, #{emotion}, #{score}, NOW())
            """)
    int insertEmotion(@Param("sessionId") Long sessionId, @Param("userId") Long userId, @Param("emotion") String emotion, @Param("score") int score);

    @Update("""
            UPDATE mh_chat_emotion
            SET dominant_emotion = #{emotion}, mood_score = #{score}, update_time = NOW()
            WHERE session_id = #{sessionId} AND user_id = #{userId}
            """)
    int updateEmotion(@Param("sessionId") Long sessionId, @Param("userId") Long userId, @Param("emotion") String emotion, @Param("score") int score);

    @Select("""
            SELECT dominant_emotion AS dominantEmotion, mood_score AS moodScore
            FROM mh_chat_emotion
            WHERE session_id = #{sessionId} AND user_id = #{userId}
            """)
    Map<String, Object> selectEmotion(@Param("sessionId") Long sessionId, @Param("userId") Long userId);
}
