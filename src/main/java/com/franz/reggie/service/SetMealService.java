package com.franz.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.franz.reggie.dto.DishDto;
import com.franz.reggie.dto.SetMealDto;
import com.franz.reggie.entity.SetMeal;

import java.util.List;

public interface SetMealService extends IService<SetMeal> {
    /**
     * 新增带菜品的套餐
     * @param setMealDto
     */
    public void saveSetMealWithDishes(SetMealDto setMealDto);

    /**
     * 获取带菜品的套餐
     * @param id
     */
    public SetMealDto getSetMealWithDishes(Long id);

    /**
     * 修改套餐
     * @param setMealDto
     */
    public void updateSetMealWithDishes(SetMealDto setMealDto);

    /**
     * 删除套餐，同时要删除setmeal_dish中对应的菜品
     */
    public void deleteSetMealWithDishes(List<Long> ids);
}
