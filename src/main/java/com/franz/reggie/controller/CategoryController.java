package com.franz.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.franz.reggie.common.Code;
import com.franz.reggie.common.Result;
import com.franz.reggie.entity.Category;
import com.franz.reggie.entity.Employee;
import com.franz.reggie.service.CategoryService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * @param category
     * @return
     */
    @PostMapping
    public Result<String> addCategory(@RequestBody Category category) {
        log.info("addCategory category: {}", category);
        categoryService.save(category);
        return Result.success("新增分类成功");
    }

    /**
     * 员工信息分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    //当请求参数与函数中参数名不一致时，使用RequestParam做映射，但最好是参数名一致
    public Result<Page> getEmployeePage(int page, int pageSize) {
        log.info("page = {}, pageSize = {}", page, pageSize);

        //构造分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        //添加排序条件
        lqw.orderByAsc(Category::getSort);

        //查询
        categoryService.page(pageInfo, lqw);

        return Result.success(pageInfo);
    }

    @DeleteMapping
    public Result<String> deleteCategory(@RequestParam Long ids) {
        log.info("删除分类 id={}", ids);

        //使用自定义的业务删除方法
        categoryService.remove(ids);
        return Result.success("删除分类成功");
    }

    @PutMapping
    public Result<String> updateCategory(@RequestBody Category category) {
        log.info("修改分类信息：{}", category);

        categoryService.updateById(category);

        return Result.success("修改分类信息成功");
    }

    /**
     * 根据条件查询分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")
    public Result<List<Category>> getCategoryByType(Category category) {
        log.info("查询分类 type = {}", category.getType() == Code.DISH_TYPE ? "菜品" : "套餐");
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.eq(category.getType() != null, Category::getType, category.getType());
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(lqw);

        return Result.success(list);
    }
}
