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
