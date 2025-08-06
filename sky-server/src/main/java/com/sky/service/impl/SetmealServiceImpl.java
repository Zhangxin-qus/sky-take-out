package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
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

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

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
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if(status == StatusConstant.ENABLE){
            //select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?
            List<Dish> dishList = dishMapper.getBySetmealId(setmealId);
            if(dishList != null && dishList.size() > 0){
                dishList.forEach(dish -> {
                    // 如果有未起售的，也就是为0的，抛出异常
                    if(StatusConstant.DISABLE == dish.getStatus()){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        // 需要修改为停售或者菜品全部为起售中才进行套餐的状态修改
        Setmeal setmeal = Setmeal.builder()
                .id(setmealId)
                .status(status)
                .build();
        setmealMapper.update(setmeal);

    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        // 获取套餐数据
        Setmeal setmeal = setmealMapper.getById(id);

        // 获取其关联的菜品套餐表数据(可能有多条数据)
        List<SetmealDish> setmealDishList = setmealDishMapper.getBySetmealId(id);

        // 拼装数据，将其返回
        SetmealVO setmealVO = new SetmealVO();
        // 拼接套餐数据
        BeanUtils.copyProperties(setmeal, setmealVO);
        // 拼接菜品套餐关系数据
        setmealVO.setSetmealDishes(setmealDishList);

        return setmealVO;
    }

    /**
     * 修改套餐和其菜品套餐关系表
     * @param setmealDTO
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        // 修改套餐数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        setmealMapper.update(setmeal);

        // 先将之前套餐id对应的菜品套餐数据删除       创建一个只包含一个元素的不可变 List（单元素列表）
        setmealDishMapper.deleteSetmealIds(Collections.singletonList(setmealDTO.getId()));

        // 修改套餐菜品关系数据
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        // 不为空则删除之前的关联数据，从新插入目前传来的新数据
        if (setmealDishList != null && !setmealDishList.isEmpty()) {
            // 为其中的每条数据赋值setmealId
            for (SetmealDish setmealDish : setmealDishList) {
                setmealDish.setSetmealId(setmeal.getId());
            }
            setmealDishMapper.insertBatch(setmealDishList);
        }

    }
}
