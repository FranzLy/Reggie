package com.franz.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.franz.reggie.common.Code;
import com.franz.reggie.common.Result;
import com.franz.reggie.entity.Employee;
import com.franz.reggie.service.EmployeeService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        /*数据库中存储的是MD5加密后密码*/
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        /*根据用户提交的username查询数据库*/
        /*
        druid版本与mybatis-plus不匹配时，会导致这里查询失败抛出异常：
        threw exception [Request processing failed:
        org.springframework.dao.InvalidDataAccessApiUsageException:
        Error attempting to get column 'create_time' from result set.
        Cause: java.sql.SQLFeatureNotSupportedException; null] with root cause
        */
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        /*未查询到用户*/
        if (emp == null) {
            return Result.error("登录失败");
        }

        /*密码不对*/
        if (!emp.getPassword().equals(password)) {
            return Result.error("登录失败");
        }

        /*禁用情形*/
        if (emp.getStatus() == 0) {
            return Result.error("用户已禁用");
        }

        /**/
        request.getSession().setAttribute(Code.EMPLOYEE, emp.getId());
        return Result.success(emp);
    }

    /**
     * 退出
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        //实际的退出是由前端处理
        request.getSession().removeAttribute(Code.EMPLOYEE);
        return Result.success("退出成功");
    }

    @PostMapping
    public Result<String> addEmployee(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("当前线程id = {}", Thread.currentThread().getId());
        log.info("新增员工，员工信息{}", employee.toString());

        //设置初始密码，MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
/*
        开启了公共字段自动填充后，这里不需要再手动填入了
        //时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //用户ID
        Long userId = (Long) request.getSession().getAttribute(Code.EMPLOYEE);
        employee.setCreateUser(userId);
        employee.setUpdateUser(userId);
*/

        //保存到数据库
        employeeService.save(employee);

        return Result.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     *
     * @param page
     * @param pageSize
     * @param employeeName
     * @return
     */
    @GetMapping("/page")
    //当请求参数与函数中参数名不一致时，使用RequestParam做映射，但最好是参数名一致
    public Result<Page> getEmployeePage(int page, int pageSize, @RequestParam(value = "name", required = false) String employeeName) {
        log.info("page = {}, pageSize = {}, employeeName = {} ", page, pageSize, employeeName);

        //构造分页构造器
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        //添加过滤条件
        lqw.like(StringUtils.isNotEmpty(employeeName), Employee::getUsername, employeeName);
        //添加排序条件
        lqw.orderByDesc(Employee::getUpdateTime);

        //查询
        employeeService.page(pageInfo, lqw);

        return Result.success(pageInfo);
    }

    /**
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public Result<String> modifyEmployee(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("当前线程id = {}", Thread.currentThread().getId());
        log.info("待修改员工的信息：{}", employee.toString());

        //如果不使用转换器的话，前端传来的Long数据可能会被截断，导致ID与数据库中不一致，从而更新查询失败
        Long employeeId = (Long) request.getSession().getAttribute(Code.EMPLOYEE);
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(employeeId);
        employeeService.updateById(employee);

        String name = employeeService.getById(employee.getId()).getName();
        return Result.success("员工" + name + "信息修改成功");
    }

    @GetMapping("{id}")
    public Result<Employee> getEmployee(@PathVariable Long id) {
        log.info("获取员工id为{}的信息", id);

        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

}
