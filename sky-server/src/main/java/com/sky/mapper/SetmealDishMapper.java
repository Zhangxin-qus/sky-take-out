package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 获取批量菜品关的联套餐总数
     * @param ids
     * @return
     */
    Integer countByDishIds(List<Long> ids);
}
