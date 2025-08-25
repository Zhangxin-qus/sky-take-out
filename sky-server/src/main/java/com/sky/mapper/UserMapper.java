package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 通过openid查询用户信息
     * @return
     */
    @Select("select id, openid, name, phone, sex, id_number, avatar, create_time " +
            "from sky_take_out.user where openid = #{openid}")
    User getByOpenid(String openid);

    void insert(User user);
}
