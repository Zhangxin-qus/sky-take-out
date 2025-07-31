package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

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
}
