package com.franz.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.franz.reggie.common.Code;
import com.franz.reggie.common.CustomException;
import com.franz.reggie.dto.DishDto;
import com.franz.reggie.dto.SetMealDto;
import com.franz.reggie.entity.Dish;
import com.franz.reggie.entity.DishFlavor;
import com.franz.reggie.entity.SetMeal;
import com.franz.reggie.entity.SetMealDish;
import com.franz.reggie.mapper.SetMealMapper;
import com.franz.reggie.service.SetMealDishService;
import com.franz.reggie.service.SetMealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetMealServiceImpl extends ServiceImpl<SetMealMapper, SetMeal> implements SetMealService {
    @Autowired
    private SetMealDishService setMealDishService;

    @Transactional
    @Override
    public void saveSetMealWithDishes(SetMealDto setMealDto) {
        //保存套餐到dish表
        this.save(setMealDto);

        //获取套餐ID，保存到每个菜品的套餐id中
        Long setMealId = setMealDto.getId();
        List<SetMealDish> setMealDishes = setMealDto.getSetMealDishes();
        setMealDishes.stream().map((item) ->{
            item.setSetmealId(setMealId);
            return item;
        }).collect(Collectors.toList());

        //保存口味到setmeal_dish表
        setMealDishService.saveBatch(setMealDishes);
    }

    @Override
    public SetMealDto getSetMealWithDishes(Long id) {
        //返回对象
        SetMealDto setMealDto = new SetMealDto();

        //获取套餐
        SetMeal setMeal = this.getById(id);
        BeanUtils.copyProperties(setMeal, setMealDto);

        //获取对应菜品
        LambdaQueryWrapper<SetMealDish> lqw = new LambdaQueryWrapper<SetMealDish>();
        lqw.eq(SetMealDish::getSetmealId, id);
        List<SetMealDish> dishes = setMealDishService.list(lqw);
        setMealDto.setSetMealDishes(dishes);

        return setMealDto;
    }

    @Transactional
    @Override
    public void updateSetMealWithDishes(SetMealDto setMealDto) {
        //更新setmeal表中字段
        this.updateById(setMealDto);

        //更新setmeal_dish中字段
        //先删除，再插入
        Long setMealId = setMealDto.getId();
        LambdaQueryWrapper<SetMealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetMealDish::getSetmealId, setMealId);
        setMealDishService.remove(lqw);
        List<SetMealDish> setMealDishes = setMealDto.getSetMealDishes();
        setMealDishes.stream().map((item) ->{
            item.setSetmealId(setMealId);
            return item;
        }).collect(Collectors.toList());
        setMealDishService.saveBatch(setMealDishes);
    }

    @Transactional
    @Override
    public void deleteSetMealWithDishes(List<Long> ids){
        //先查询套餐是否正在售卖中，若是，则不能删除
        //select count(*) from setmeal where id in ids and status = 1
        LambdaQueryWrapper<SetMeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(SetMeal::getId, ids);
        lqw.eq(SetMeal::getStatus, Code.DISH_STATUS_SEALING);
        long count = this.count(lqw);
        if(count > 0){
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        //删除套餐
        this.removeBatchByIds(ids);

        //删除setmeal_dish中的菜品
        LambdaQueryWrapper<SetMealDish> lqwDish = new LambdaQueryWrapper<>();
        lqwDish.in(SetMealDish::getSetmealId, ids);
        setMealDishService.remove(lqwDish);
    }
}
