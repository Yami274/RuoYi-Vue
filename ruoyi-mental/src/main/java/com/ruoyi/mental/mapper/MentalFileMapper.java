package com.ruoyi.mental.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MentalFileMapper
{
    @Insert("""
            INSERT INTO mh_file_record(business_type, business_id, business_field, origin_name, file_name, file_url,
                                       file_size, uploader_id, uploader_name, create_time)
            VALUES(#{businessType}, #{businessId}, #{businessField}, #{originName}, #{fileName}, #{fileUrl},
                   #{fileSize}, #{uploaderId}, #{uploaderName}, NOW())
            """)
    int insertFileRecord(@Param("businessType") String businessType, @Param("businessId") String businessId,
            @Param("businessField") String businessField, @Param("originName") String originName,
            @Param("fileName") String fileName, @Param("fileUrl") String fileUrl, @Param("fileSize") long fileSize,
            @Param("uploaderId") Long uploaderId, @Param("uploaderName") String uploaderName);
}
