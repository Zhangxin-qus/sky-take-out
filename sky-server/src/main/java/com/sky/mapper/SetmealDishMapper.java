package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 获取批量菜品关的联套餐总数
     * @param ids
     * @return
     */
    Integer countByDishIds(List<Long> ids);

    /**
     * 批量插入套餐菜品关系表
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 批量删除套餐关联数据
     * @param ids
     */
    void deleteSetmealIds(List<Long> ids);

    /**
     * 启售停售套餐
     * @param status
     */
    @Update("update sky_take_out.setmeal set status = #{status} where id = #{setmealId}")
    void startOrStop(Integer status, Long setmealId);
}
