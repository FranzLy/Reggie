package com.franz.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.franz.reggie.common.CustomException;
import com.franz.reggie.entity.Category;
import com.franz.reggie.entity.Dish;
import com.franz.reggie.entity.SetMeal;
import com.franz.reggie.mapper.CategoryMapper;
import com.franz.reggie.mapper.DishMapper;
import com.franz.reggie.service.CategoryService;
import com.franz.reggie.service.DishService;
import com.franz.reggie.service.SetMealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetMealService setMealService;

    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLqw = new LambdaQueryWrapper<>();

        //添加查询条件
        dishLqw.eq(Dish::getCategoryId, id);
        int dishCount = (int) dishService.count(dishLqw);
        //查询当前分类是否关联了菜品，若关联，则不允许删除
        if (dishCount > 0) {
            throw new CustomException("当前分类关联了菜品，不能删除");
        }

        LambdaQueryWrapper<SetMeal> setMealLqw = new LambdaQueryWrapper<>();
        setMealLqw.eq(SetMeal::getCategoryId, id);
        int setMealCount = (int) setMealService.count(setMealLqw);
        //查询当前分类是否关联了套餐，若关联，则不允许删除
        if (setMealCount > 0) {
            throw new CustomException("当前分类关联了套餐，不能删除");
        }

        //正常删除
        super.removeById(id);
    }
}
