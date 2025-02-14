package com.franz.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.franz.reggie.common.Code;
import com.franz.reggie.common.CustomException;
import com.franz.reggie.dto.DishDto;
import com.franz.reggie.entity.Dish;
import com.franz.reggie.entity.DishFlavor;
import com.franz.reggie.mapper.DishMapper;
import com.franz.reggie.service.DishFlavorService;
import com.franz.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * @param dishDto
     */
    @Transactional
    @Override
    public void saveDishWithFlavor(DishDto dishDto) {
        //保存菜品到dish表
        this.save(dishDto);

        //获取菜品ID，保存到每个口味的菜品id中
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item) ->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存口味到dish_flavor表
        dishFlavorService.saveBatch(flavors);
    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public DishDto getDishWithFlavor(Long id) {
        //返回对象
        DishDto dishDto = new DishDto();

        //获取菜品
        Dish dish = this.getById(id);
        BeanUtils.copyProperties(dish, dishDto);

        //获取口味
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<DishFlavor>();
        lqw.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorService.list(lqw);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Transactional
    @Override
    public void updateDishWithFlavor(DishDto dishDto) {
        //更新dish表中字段
        this.updateById(dishDto);

        //更新dish_flavor中字段
        //先删除，再插入
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(lqw);
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item) ->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void deleteDishWithFlavor(List<Long> ids){
        //先查是否可删除，售卖中不可删除
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.in(Dish::getId, ids);
        lqw.eq(Dish::getStatus, Code.DISH_STATUS_SEALING);
        long count = this.count(lqw);
        if(count > 0){
            throw new CustomException("菜品正在售卖中，不能删除");
        }

        //删除
        this.removeBatchByIds(ids);

        //删除对应口味
        LambdaQueryWrapper<DishFlavor> lqwFlavor = new LambdaQueryWrapper<>();
        lqwFlavor.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(lqwFlavor);
    }
}
