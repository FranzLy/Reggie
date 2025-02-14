package com.franz.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.franz.reggie.common.CustomException;
import com.franz.reggie.entity.*;
import com.franz.reggie.mapper.OrderMapper;
import com.franz.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Transactional
    @Override
    public void submitOrder(Orders order) {
        //获取用户id
        Long userId = order.getUserId();

        //查询当前用户车数据
        LambdaQueryWrapper<ShoppingCart> lqwCart = new LambdaQueryWrapper<>();
        lqwCart.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shopCartList = shoppingCartService.list(lqwCart);
        if(shopCartList == null || shopCartList.size() == 0){
            String msg = "用户"+userId+"购物车为空，不能下单";
            log.warn(msg);
            throw new CustomException(msg);
        }

        //获取用户
        User user = userService.getById(userId);

        //获取地址
        Long addressBookId = order.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook == null){
            throw new CustomException("地址信息有误，无法下单");
        }

        //向订单表插入一条数据
        Long orderId = IdWorker.getId();        //生成订单号
        AtomicInteger amount = new AtomicInteger(0);//原子操作计算金额
        List<OrderDetail> orderDetails = shopCartList.stream().map((item) -> {//设置订单明细
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        //设置订单信息
        order.setId(orderId);
        order.setOrderTime(LocalDateTime.now());
        order.setCheckoutTime(LocalDateTime.now());
        order.setStatus(2);
        order.setAmount(new BigDecimal(amount.get()));//总金额
        order.setUserId(userId);
        order.setNumber(String.valueOf(orderId));
        order.setUserName(user.getName());
        order.setConsignee(addressBook.getConsignee());
        order.setPhone(addressBook.getPhone());
        order.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入数据，一条数据
        this.save(order);

        //向订单明细表插入数据，可能多条
        orderDetailService.saveBatch(orderDetails);

        //删除对应购物车
        shoppingCartService.remove(lqwCart);
    }
}
