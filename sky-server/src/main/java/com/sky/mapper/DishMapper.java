package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增菜品和对应的口味
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void inset(Dish dish);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);


    /**
     * 获取批量菜品的启用数
     * @param ids
     */
    Integer countByIdsAndStatus(List<Long> ids);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据id查询菜品数据
     * @param id
     * @return
     */
    @Select("select * from sky_take_out.dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 根据id修改菜品数据
     * @param dish
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 启用禁用菜品
     * @param status
     * @param id
     */
    @Update("update sky_take_out.dish set status = #{status} where id = #{id}")
    void startOrStop(Integer status, Long id);

    /**
     * 根据菜品id查询对应的口味
     * @param dish
     * @return
     */
    List<Dish> list(Dish dish);
}
