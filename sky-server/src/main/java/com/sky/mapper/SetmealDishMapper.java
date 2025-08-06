package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
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
     * 根据id查询菜品套餐关系数据
     * @param id
     * @return
     */
    @Select("select id, setmeal_id, dish_id, name, price, copies " +
            "from sky_take_out.setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getBySetmealId(Long id);

    /**
     * 根据菜品id获取套餐id，用于菜品和对应套餐的同时停售
     * @param id
     * @return
     */
    @Select("select setmeal_id from sky_take_out.setmeal_dish where dish_id = #{id}")
    List<Long> getByDishId(Long id);
}
