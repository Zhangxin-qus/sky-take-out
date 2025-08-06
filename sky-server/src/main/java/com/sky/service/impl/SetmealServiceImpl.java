package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增套餐和对应的套餐菜品关系表
     * @param setmealDTO
     */
    @Override
    @Transactional // 加上事务一致性
    public void save(SetmealDTO setmealDTO) {
        // 使用entity实体类，得到公共字段的属性
        // 插入套餐数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.inset(setmeal);

        // 插入套餐菜品关系数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        // 获取insert语句插入成功后获得的id值
        Long setmealId = setmeal.getId();

        // 为其添加setmealID（在插入套餐数据的xml文件中设置主键返回功能，并返回到其id属性上）
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            for (SetmealDish setmealDish : setmealDishes) {
                // 为每条数据添加套餐id
                setmealDish.setSetmealId(setmealId);
            }
            setmealDishMapper.insertBatch(setmealDishes);
        }

    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        // 使用pageHelper进行分页查询
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    public void delete(List<Long> ids) {
        // 先判断该批套餐中是否有在起售中的，有的话不准删除
        Integer count = setmealMapper.countByIdsAndStatus(ids);
        if (count > 0) {
            // 有起售的套餐，则不能删除，可以抛出异常
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }

        // 如果没有起售中的，那么就删除套餐，并且将菜品套餐关系表对应数据也删除掉
        setmealMapper.deleteByIds(ids);
        setmealDishMapper.deleteSetmealIds(ids);

    }

    /**
     * 启售停售套餐
     * @param status
     */
    @Override
    public void startOrStop(Integer status, Long setmealId) {
        setmealDishMapper.startOrStop(status, setmealId);
    }
}
