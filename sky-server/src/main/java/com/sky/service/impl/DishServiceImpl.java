package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Override
    @Transactional // 加上事务一致性注解
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();

        // 使用对象拷贝，获得dish对象相同属性进行插入（便于携带时间数据等）
        BeanUtils.copyProperties(dishDTO, dish);

        // 每次向菜品表添加一条数据
        dishMapper.inset(dish);

        // 获取insert语句插入成功后获得的id值
        Long dishId = dish.getId();

        // 就会向口味表添加n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 判断口味是否存在，因为口味不是必须的
        if (flavors != null && flavors.size() > 0) {
            for (DishFlavor f : flavors) {
                // 将每一条数据的dishID都赋值上
                f.setDishId(dishId);
            }

            // 向口味表批量插入n条数据（不需要先遍历再插入，在mapper映射文件中使用动态sql整体插入即可）
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        // 使用pageHelper分页查询
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        // 接收返回值
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        // 返回PageResult对象
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 判断当前菜品能否删除--是否在起售中的
        Integer count = dishMapper.countByIdsAndStatus(ids);
        if (count > 0) {
            // 存在起售中的菜品
            log.info("存在起售数：{}", count);
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }

        // 菜品是否被套餐关联--是则不能删除
        Integer setmealCount = setmealDishMapper.countByDishIds(ids);
        if (setmealCount > 0) {
            // 存在套餐关联
            log.info("存在套餐关联数：{}", setmealCount);
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品数据
        dishMapper.deleteByIds(ids);

        // 删除菜品关联的口味数据
        dishFlavorMapper.deleteDishIds(ids);

    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        // 根据id查询菜品数据
        Dish dish = dishMapper.getById(id);

        // 根据菜品id查询口味数据(可能对应有多条口味)
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        // 将查询到数据封装到VO
        DishVO dishVO = new DishVO();
        // 使用对象拷贝
        BeanUtils.copyProperties(dish, dishVO);
        // 再设置口味数据
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    /**
     * 根据id修改菜品基本信息和对应的口味信息
     * @param dishDTO
     */
    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        // 修改菜品表数据
        // 转为entity实体类型，用于公共字段填充
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        // 修改菜品对应的口味
        // 先将之前菜品id对应的口味删除       创建一个只包含一个元素的不可变 List（单元素列表）
        dishFlavorMapper.deleteDishIds(Collections.singletonList(dishDTO.getId()));

        // 再插入新的口味列表
        // 获取name和value
        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 判断是否为空，不为空，批量插入口味
        if (flavors != null && !flavors.isEmpty()) {
            // 不为空后先将dishId插入列表中各个对象，用于后续的批量添加口味
            for (DishFlavor f : flavors) {
                f.setDishId(dishDTO.getId());
            }
            // 批量插入口味
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 启用禁用菜品
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        dishMapper.startOrStop(status, id);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
        // 欲获取对应分类菜品，得查询对应的分类id和判断其是否启用：1
        Dish dish = new Dish().builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();

        List<Dish> dishList = dishMapper.list(dish);

        return dishList;
    }
}
