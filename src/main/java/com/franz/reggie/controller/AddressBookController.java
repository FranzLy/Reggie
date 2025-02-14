package com.franz.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.franz.reggie.common.BaseContext;
import com.franz.reggie.common.Result;
import com.franz.reggie.entity.AddressBook;
import com.franz.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.zone.ZoneRules;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增
     *
     * @return
     */
    @PostMapping
    public Result<AddressBook> addAddressBook(@RequestBody AddressBook addressBook) {
        log.info("添加地址簿: {}", addressBook);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookService.save(addressBook);
        return Result.success(addressBook);
    }

    /**
     * 列出所有地址
     *
     * @param addressBook
     * @return
     */
    @GetMapping("/list")
    public Result<List<AddressBook>> listAddressBook(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("查询所有地址：{}", addressBook);

        //SQL:select * from address_book where user_id = ? order by update_time desc
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<AddressBook>();
        lqw.eq(AddressBook::getUserId, addressBook.getUserId());
        lqw.orderByDesc(AddressBook::getUpdateTime);
        List<AddressBook> list = addressBookService.list(lqw);
        return Result.success(list);
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public Result<String> setAsDefault(@RequestBody AddressBook addressBook) {
        log.info("将{}设为默认地址", addressBook);

        //先将所有地址都改成不是默认，再将改地址改为默认
        //SQL:update address_book set is_default = 0 where user_id = ?
        LambdaUpdateWrapper<AddressBook> lqw = new LambdaUpdateWrapper<AddressBook>();
        lqw.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        lqw.set(AddressBook::getIsDefault, 0);
        addressBookService.update(lqw);

        //SQL:update address_book set is_default = 1 where id = ?
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);

        return Result.success("获取默认地址成功");
    }

    /**
     * 获取默认地址
     */
    @GetMapping("/default")
    public Result getDefaultAddressBook() {
        //SQL:select * from address_book where user_id = ? and is_default = 1
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper();
        lqw.eq(AddressBook::getIsDefault, 1);
        lqw.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        AddressBook addressBook = addressBookService.getOne(lqw);

        return addressBook != null ? Result.success(addressBook) : Result.error("没找到该对象");
    }

    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    public Result get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return Result.success(addressBook);
        } else {
            return Result.error("没有找到该对象");
        }
    }
}
