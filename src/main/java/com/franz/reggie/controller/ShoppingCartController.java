package com.franz.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.franz.reggie.common.BaseContext;
import com.franz.reggie.common.Result;
import com.franz.reggie.entity.ShoppingCart;
import com.franz.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("加入购物车：{}", shoppingCart.toString());
        //获取用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        //查询当前菜品/套餐是否已在该用户购物车内
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);

        Long dishId = shoppingCart.getDishId();
        if (dishId != null) {
            //菜品
            lqw.eq(ShoppingCart::getDishId, dishId);
        } else {
            //套餐
            lqw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart item = shoppingCartService.getOne(lqw);

        //若已存在，则加一
        //否则添加，数量默认为1
        if (item != null) {
            Integer number = item.getNumber();
            item.setNumber(number + 1);
            shoppingCartService.updateById(item);
        } else {
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
        }

        //添加创建时间
        shoppingCart.setCreateTime(LocalDateTime.now());

        return Result.success(shoppingCart);
    }


    /**
     * 查看购物车
     *
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        log.info("查看购物车");

        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        lqw.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(lqw);

        return Result.success(list);
    }

    /**
     * 清空购物车
     *
     * @return
     */
    @DeleteMapping("/clean")
    public Result<String> clean() {
        log.info("清空购物车");

        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        shoppingCartService.remove(lqw);

        return Result.success("购物车清空成功");
    }

    /**
     * 购物车减项
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public Result<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        log.info("减少菜品或套餐：{}", shoppingCart.toString());

        //根据用户id和菜品、套餐id查
        Long userId = BaseContext.getCurrentId();
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        if (dishId != null) {
            lqw.eq(ShoppingCart::getDishId, dishId);
        } else {
            lqw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        //若数量等于1，则移除，否则减一后更新
        ShoppingCart item = shoppingCartService.getOne(lqw);
        Integer number = item.getNumber();
        item.setNumber(number - 1);
        if (item.getNumber() == 0) {
            shoppingCartService.remove(lqw);
        } else {
            shoppingCartService.update(item, lqw);
        }

        return Result.success(item);
    }
}
