package com.franz.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.franz.reggie.common.Code;
import com.franz.reggie.common.Result;
import com.franz.reggie.dto.DishDto;
import com.franz.reggie.entity.Category;
import com.franz.reggie.entity.Dish;
import com.franz.reggie.entity.DishFlavor;
import com.franz.reggie.service.CategoryService;
import com.franz.reggie.service.DishFlavorService;
import com.franz.reggie.service.DishService;
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
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 分页获取信息
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    //当请求参数与函数中参数名不一致时，使用RequestParam做映射，但最好是参数名一致
    public Result<Page> getDishes(int page, int pageSize, String name) {
        log.info("菜品 page = {}, pageSize = {}, name = {}", page, pageSize, name);

        //构造分页构造器
        //Dish中不包含菜品分类信息，但前端需要展示，所以用传输数据进行封装
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPageInfo = new Page<>(page, pageSize);

        //条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();

        //添加过滤条件
        lqw.like(StringUtils.isNotEmpty(name), Dish::getName, name);

        //添加排序条件
        lqw.orderByAsc(Dish::getSort);

        //查询
        dishService.page(pageInfo, lqw);

        //对象拷贝，暂时不拷贝实际的数据，因为缺少分类信息，所以在下面处理
        BeanUtils.copyProperties(pageInfo, dishDtoPageInfo, "records");

        //查询分类对象，并填充分类名称
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dto = new DishDto();
            BeanUtils.copyProperties(item, dto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            dto.setCategoryName(category.getName());

            return dto;
        }).collect(Collectors.toList());
        dishDtoPageInfo.setRecords(list);

        return Result.success(dishDtoPageInfo);
    }

    /**
     * [批量]修改状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public Result<String> modifyDishStatus(@PathVariable Integer status, @RequestParam List<Long> ids) {
        log.info("修改菜品状态至 status = {}, ids = {}", status, ids);
        dishService.lambdaUpdate().set(Dish::getStatus, status).in(Dish::getId, ids).update();
        return Result.success("菜品状态修改成功");
    }

    /**
     * [批量]删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> deleteDish(@RequestParam List<Long> ids) {
        log.info("删除菜品 ids = {}", ids);
        dishService.deleteDishWithFlavor(ids);
        return Result.success("菜品删除成功");
    }

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public Result<String> addDish(@RequestBody DishDto dishDto) {
        log.info("新增菜品：{}", dishDto.toString());
        dishService.saveDishWithFlavor(dishDto);
        return Result.success("菜品添加成功");
    }


    /**
     * 获取单条菜品，修改前前端会调用
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishDto> getDish(@PathVariable Long id) {
        log.info("菜品 id = {}", id);
        DishDto dishDto = dishService.getDishWithFlavor(id);
        return Result.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public Result<String> modifyDish(@RequestBody DishDto dishDto) {
        log.info("修改菜品：{}", dishDto.toString());
        dishService.updateDishWithFlavor(dishDto);
        return Result.success("菜品修改成功");
    }

//    /**
//     * 获取菜品，在套餐新增/修改页面会调用
//     * @param dish
//     * @return
//     */
//    @GetMapping("/list")
//    public Result<List<Dish>> getDishes(Dish dish) {
//        log.info("获取菜品， id = {}", dish.getCategoryId());
//        Long categoryId = dish.getCategoryId();
//        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
//        lqw.eq(categoryId != null, Dish::getCategoryId, categoryId).eq(Dish::getStatus, Code.DISH_STATUS_SEALING);
//        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(lqw);
//
//        return Result.success(list);
//    }

        /**
     * 获取菜品，在套餐新增/修改页面会调用
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public Result<List<DishDto>> getDishes(Dish dish) {
        log.info("获取菜品， id = {}", dish.getCategoryId());
        Long categoryId = dish.getCategoryId();
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(categoryId != null, Dish::getCategoryId, categoryId).eq(Dish::getStatus, Code.DISH_STATUS_SEALING);
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(lqw);

        List<DishDto> dishDtoList = list.stream().map((item) ->{
            DishDto dto = new DishDto();
            BeanUtils.copyProperties(item, dto);

            //select * from dish_flavor where dish_id = ?
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lqwFlavor = new LambdaQueryWrapper<>();
            lqwFlavor.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> flavors = dishFlavorService.list(lqwFlavor);
            dto.setFlavors(flavors);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dishDtoList);
    }

}
