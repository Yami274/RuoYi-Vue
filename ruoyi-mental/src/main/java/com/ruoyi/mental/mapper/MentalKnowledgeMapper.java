package com.ruoyi.mental.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MentalKnowledgeMapper
{
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
}
