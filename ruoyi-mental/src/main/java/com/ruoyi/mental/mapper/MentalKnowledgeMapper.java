package com.ruoyi.mental.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MentalKnowledgeMapper
{
    @Select("""
            <script>
            SELECT category_name AS categoryName, COUNT(1) AS articleCount
            FROM mh_knowledge_article
            WHERE del_flag = '0'
              AND category_name IS NOT NULL
              AND category_name != ''
            GROUP BY category_name
            ORDER BY category_name ASC
            </script>
            """)
    List<Map<String, Object>> selectCategorySummary();

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM mh_knowledge_article
            WHERE del_flag = '0'
              <if test='title != null and title != ""'>
                AND title LIKE CONCAT('%', #{title}, '%')
              </if>
              <if test='categoryName != null and categoryName != ""'>
                AND category_name = #{categoryName}
              </if>
              <if test='status != null and status != ""'>
                AND status = #{status}
              </if>
              <if test='authorName != null and authorName != ""'>
                AND author LIKE CONCAT('%', #{authorName}, '%')
              </if>
            </script>
            """)
    long countArticlesForAdmin(@Param("title") String title, @Param("categoryName") String categoryName,
            @Param("status") String status, @Param("authorName") String authorName);

    @Select("""
            <script>
            SELECT id,
                   title,
                   summary,
                   content,
                   category_name AS categoryName,
                   cover,
                   author,
                   read_count AS readCount,
                   like_count AS likeCount,
                   comment_count AS commentCount,
                   publish_time AS publishTime,
                   status,
                   create_time AS createTime,
                   update_time AS updateTime
            FROM mh_knowledge_article
            WHERE del_flag = '0'
              <if test='title != null and title != ""'>
                AND title LIKE CONCAT('%', #{title}, '%')
              </if>
              <if test='categoryName != null and categoryName != ""'>
                AND category_name = #{categoryName}
              </if>
              <if test='status != null and status != ""'>
                AND status = #{status}
              </if>
              <if test='authorName != null and authorName != ""'>
                AND author LIKE CONCAT('%', #{authorName}, '%')
              </if>
            ORDER BY ${sortClause}
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectArticlesForAdmin(@Param("title") String title, @Param("categoryName") String categoryName,
            @Param("status") String status, @Param("authorName") String authorName, @Param("offset") int offset,
            @Param("size") int size, @Param("sortClause") String sortClause);

    @Select("""
            SELECT COUNT(1)
            FROM mh_knowledge_article
            WHERE status = '1' AND del_flag = '0'
            """)
    long countPublishedArticles();

    @Select("""
            SELECT id,
                   title,
                   summary,
                   content,
                   category_name AS categoryName,
                   cover,
                   read_count AS readCount,
                   create_time AS createTime,
                   update_time AS updateTime
            FROM mh_knowledge_article
            WHERE status = '1' AND del_flag = '0'
            ORDER BY ${sortClause}
            LIMIT #{offset}, #{size}
            """)
    List<Map<String, Object>> selectPublishedArticles(@Param("offset") int offset, @Param("size") int size, @Param("sortClause") String sortClause);

    @Insert("""
            INSERT INTO mh_knowledge_article(title, summary, content, category_name, cover, author, read_count,
                                             like_count, comment_count, publish_time, status, create_time,
                                             update_time, del_flag)
            VALUES(#{title}, #{summary}, #{content}, #{categoryName}, #{cover}, #{author}, 0,
                   0, 0, NOW(), #{status}, NOW(), NOW(), '0')
            """)
    int insertArticle(@Param("title") String title, @Param("summary") String summary, @Param("content") String content,
            @Param("categoryName") String categoryName, @Param("cover") String cover, @Param("author") String author,
            @Param("status") String status);

    @Update("""
            UPDATE mh_knowledge_article
            SET title = #{title},
                summary = #{summary},
                content = #{content},
                category_name = #{categoryName},
                cover = #{cover},
                update_time = NOW()
            WHERE id = #{id} AND del_flag = '0'
            """)
    int updateArticle(@Param("id") Long id, @Param("title") String title, @Param("summary") String summary,
            @Param("content") String content, @Param("categoryName") String categoryName, @Param("cover") String cover);

    @Update("""
            UPDATE mh_knowledge_article
            SET status = #{status},
                update_time = NOW(),
                publish_time = CASE WHEN #{status} = '1' THEN NOW() ELSE publish_time END
            WHERE id = #{id} AND del_flag = '0'
            """)
    int updateArticleStatus(@Param("id") Long id, @Param("status") String status);

    @Update("""
            UPDATE mh_knowledge_article
            SET del_flag = '2', update_time = NOW()
            WHERE id = #{id} AND del_flag = '0'
            """)
    int deleteArticle(@Param("id") Long id);

    @Select("""
            SELECT id,
                   title,
                   summary,
                   content,
                   category_name AS categoryName,
                   cover,
                   author,
                   read_count AS readCount,
                   like_count AS likeCount,
                   comment_count AS commentCount,
                   publish_time AS publishTime,
                   status,
                   create_time AS createTime,
                   update_time AS updateTime
            FROM mh_knowledge_article
            WHERE id = #{id} AND del_flag = '0'
            """)
    Map<String, Object> selectArticleDetailForAdmin(@Param("id") Long id);

    @Select("""
            SELECT id,
                   title,
                   summary,
                   content,
                   category_name AS categoryName,
                   cover,
                   author,
                   read_count AS readCount,
                   like_count AS likeCount,
                   comment_count AS commentCount,
                   publish_time AS publishTime,
                   create_time AS createTime,
                   update_time AS updateTime
            FROM mh_knowledge_article
            WHERE id = #{id} AND status = '1' AND del_flag = '0'
            """)
    Map<String, Object> selectArticleDetail(@Param("id") Long id);

    @Update("""
            UPDATE mh_knowledge_article
            SET read_count = read_count + 1, update_time = NOW()
            WHERE id = #{id} AND status = '1' AND del_flag = '0'
            """)
    int increaseReadCount(@Param("id") Long id);

    @Select("""
            SELECT COUNT(1) FROM sys_user WHERE del_flag = '0'
            """)
    long countTotalUsers();

    @Select("""
            SELECT COUNT(1) FROM mh_chat_session WHERE del_flag = '0'
            """)
    long countTotalSessions();

    @Select("""
            SELECT COUNT(1) FROM mh_emotion_diary WHERE del_flag = '0'
            """)
    long countTotalDiaries();

    @Select("""
            SELECT IFNULL(ROUND(AVG(mood_score), 2), 0) FROM mh_emotion_diary WHERE del_flag = '0'
            """)
    double avgMoodScore();

    @Select("""
            SELECT COUNT(DISTINCT user_id)
            FROM mh_chat_session
            WHERE del_flag = '0'
            """)
    long countActiveUsers();

    @Select("""
            SELECT COUNT(1)
            FROM mh_emotion_diary
            WHERE del_flag = '0' AND DATE(create_time) = CURDATE()
            """)
    long countTodayDiaries();

    @Select("""
            SELECT COUNT(1)
            FROM mh_chat_session
            WHERE del_flag = '0' AND DATE(create_time) = CURDATE()
            """)
    long countTodaySessions();

    @Select("""
            SELECT dominant_emotion AS emotion, COUNT(1) AS count
            FROM mh_emotion_diary
            WHERE del_flag = '0'
            GROUP BY dominant_emotion
            ORDER BY count DESC
            """)
    List<Map<String, Object>> selectEmotionDistribution();

    @Select("""
            <script>
            SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date,
                   IFNULL(ROUND(AVG(mood_score), 2), 0) AS avgMoodScore,
                   COUNT(1) AS diaryCount
            FROM mh_emotion_diary
            WHERE del_flag = '0'
              AND create_time &gt;= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)
            GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')
            ORDER BY date ASC
            </script>
            """)
    List<Map<String, Object>> selectEmotionTrend(@Param("days") int days);

    @Select("""
            <script>
            SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date,
                   COUNT(1) AS sessionCount,
                   COUNT(DISTINCT user_id) AS userCount
            FROM mh_chat_session
            WHERE del_flag = '0'
              AND create_time &gt;= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)
            GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')
            ORDER BY date ASC
            </script>
            """)
    List<Map<String, Object>> selectConsultationTrend(@Param("days") int days);
}
