package com.franz.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.franz.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {
    /**
     * 下单
     * @param order
     */
    public void submitOrder(Orders order);
}
