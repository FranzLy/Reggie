package com.franz.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.franz.reggie.common.Result;
import com.franz.reggie.dto.DishDto;
import com.franz.reggie.dto.SetMealDto;
import com.franz.reggie.entity.Category;
import com.franz.reggie.entity.Dish;
import com.franz.reggie.entity.SetMeal;
import com.franz.reggie.mapper.SetMealMapper;
import com.franz.reggie.service.CategoryService;
import com.franz.reggie.service.SetMealService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetMealController {
    @Autowired
    private SetMealService setMealService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 分页获取信息
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> getSetMeals(int page, int pageSize, String name) {
        log.info("套餐 page = {}, pageSize = {}, name = {}", page, pageSize, name);

        //构造分页构造器
        Page<SetMeal> pageInfo = new Page<>(page, pageSize);
        Page<SetMealDto> dtoPageInfo = new Page<>(page, pageSize);

        //条件构造器
        LambdaQueryWrapper<SetMeal> lqw = new LambdaQueryWrapper<>();

        //添加过滤条件
        lqw.like(StringUtils.isNotEmpty(name), SetMeal::getName, name);
        lqw.orderByDesc(SetMeal::getUpdateTime);

        //查询
        setMealService.page(pageInfo, lqw);

        //拷贝
        BeanUtils.copyProperties(dtoPageInfo, SetMealDto.class, "records");

        //封装分类信息后再拷贝records字段
        List<SetMeal> records = pageInfo.getRecords();
        List<SetMealDto> list = records.stream().map((item) -> {
            SetMealDto dto = new SetMealDto();
            BeanUtils.copyProperties(item, dto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            dto.setCategoryName(category.getName());

            return dto;
        }).collect(Collectors.toList());

        //塞入records
        dtoPageInfo.setRecords(list);
        return Result.success(dtoPageInfo);
    }

    /**
     * [批量]修改状态
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public Result<String> modifyDishStatus(@PathVariable Integer status, @RequestParam List<Long> ids) {
        log.info("修改套餐状态至 status = {}, ids = {}", status, ids);
        setMealService.lambdaUpdate().set(SetMeal::getStatus, status).in(SetMeal::getId, ids).update();
        return Result.success("套餐状态修改成功");
    }

    /**
     * 新增套餐
     *
     * @param setMealDto
     * @return
     */
    @PostMapping
    public Result<String> addSetMeal(@RequestBody SetMealDto setMealDto) {
        log.info("新增套餐：{}", setMealDto.toString());
        setMealService.saveSetMealWithDishes(setMealDto);
        return Result.success("套餐添加成功");
    }

    /**
     * 获取单条套餐，修改前前端会调用
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SetMealDto> getSetMeal(@PathVariable Long id) {
        log.info("套餐 id = {}", id);
        SetMealDto setMealDto = setMealService.getSetMealWithDishes(id);
        return Result.success(setMealDto);
    }

    /**
     * 修改套餐
     *
     * @param setMealDto
     * @return
     */
    @PutMapping
    public Result<String> modifySetMeal(@RequestBody SetMealDto setMealDto) {
        log.info("修改套餐：{}", setMealDto.toString());
        setMealService.updateSetMealWithDishes(setMealDto);
        return Result.success("套餐修改成功");
    }

    /**
     * [批量]删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> deleteSetMeal(@RequestParam List<Long> ids) {
        log.info("删除套餐 ids = {}", ids);
        setMealService.deleteSetMealWithDishes(ids);
        return Result.success("套餐删除成功");
    }

    @GetMapping("/list")
    public Result<List<SetMeal>> listSetMeals(SetMeal setMeal) {
        log.info("查询套餐：{}", setMeal.toString());

        LambdaQueryWrapper<SetMeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(setMeal.getCategoryId() != null, SetMeal::getCategoryId, setMeal.getCategoryId());
        lqw.eq(setMeal.getStatus() != null, SetMeal::getStatus, setMeal.getStatus());
        lqw.orderByDesc(SetMeal::getUpdateTime);
        List<SetMeal> setMeals = setMealService.list(lqw);

        return Result.success(setMeals);
    }
}
