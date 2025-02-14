package com.franz.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.franz.reggie.common.BaseContext;
import com.franz.reggie.common.Result;
import com.franz.reggie.entity.Employee;
import com.franz.reggie.entity.Orders;
import com.franz.reggie.entity.User;
import com.franz.reggie.service.OrderService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 提交支付
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders) {
        log.info("用户下单了：{}", orders.toString());
        orders.setUserId(BaseContext.getCurrentId());
        orderService.submitOrder(orders);
        return Result.success("下单成功");
    }

    @GetMapping("/userPage")
    public Result<Page> userPage(int page, int pageSize) {
        log.info("page = {}, pageSize = {}", page, pageSize);

        //构造分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        //添加排序条件
        lqw.orderByDesc(Orders::getOrderTime);

        //查询
        orderService.page(pageInfo, lqw);

        return Result.success(pageInfo);
    }
}
