package com.sky.service;

import com.sky.dto.DishDTO;

public interface DishService {

    /**
     * 新增菜品和对应的口味数据
     * @param dishDTO
     */
    // 除了新增菜品，还要将口味表一起完成
    void saveWithFlavor(DishDTO dishDTO);

}
