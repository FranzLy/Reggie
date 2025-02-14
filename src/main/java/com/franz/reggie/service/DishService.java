package com.franz.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.franz.reggie.dto.DishDto;
import com.franz.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    /**
     * 新增带口味的菜品
     * @param dishDto
     */
    public void saveDishWithFlavor(DishDto dishDto);

    /**
     * 获取带口味的菜品
     * @param id
     */
    public DishDto getDishWithFlavor(Long id);

    /**
     * 修改菜品
     * @param dishDto
     */
    public void updateDishWithFlavor(DishDto dishDto);

    /**
     * 删除菜品
     * @param ids
     */
    public void deleteDishWithFlavor(List<Long> ids);
}
