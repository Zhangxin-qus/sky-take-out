package com.sky.service.impl;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param dto
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO dto) {
        // 判断当前加入到购物车中的商品是否已经存在了
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(dto, shoppingCart);
        // 设置用户id
        shoppingCart.setUserId(BaseContext.getCurrentUserId());

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        // 如果已经存在了，只需要将数量加一
        if (list != null && list.size() > 0) {
            // 通过以上多条件判断，列表中只可能是一条数据，所以直接取第一条
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        }else {
            // 如果不存在，需要插入一条购物车数据

            // 判断本次添加到购物车的菜品还是套餐
            Long dishId = shoppingCart.getDishId();
            if (dishId != null) {
                // 本次添加到购物车的是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());

            }else {
                // 依然以上不为菜品，则必为套餐。本次添加到购物车的是套餐
                Long setmealId = shoppingCart.getSetmealId();

                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());

            }
            // 上面已经除去了二次添加，现在都是第一次添加，所有数量固定1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }



    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        // 解析出当前的用户id，再查询所有购物车信息
        Long userId = BaseContext.getCurrentUserId();

        // 已经使用@Accessors(chain = true)开启链式编程
        List<ShoppingCart> list = shoppingCartMapper.list(new ShoppingCart().setUserId(userId));

        return list;
    }

    /**
     * 清空购物车
     */
    @Override
    public void cleanShoppingCart() {
        // 传入userId删除全部购物车数据
        shoppingCartMapper.deleteByUserId(new ShoppingCart().setUserId(BaseContext.getCurrentUserId()));
    }

    /**
     * 删除购物车中一个商品
     * @param dto
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO dto) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(dto, shoppingCart);
        // 拼接userId
        shoppingCart.setUserId(BaseContext.getCurrentUserId());
        // 查询购物车数据
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        // 有数据则进行删除操作
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            // 数量等于1则直接删除此条数据
            if (cart.getNumber() == 1) {
                shoppingCartMapper.deleteById(cart);
            }else {
                // 如果数量大于1，则在原有基础上减1
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(cart);
            }
        }

    }
}
