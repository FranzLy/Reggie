package com.franz.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.franz.reggie.common.Code;
import com.franz.reggie.common.Result;
import com.franz.reggie.entity.User;
import com.franz.reggie.service.UserService;
import com.franz.reggie.utils.SMSUtils;
import com.franz.reggie.utils.ValidateCodeUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @param map 存储前端传过来的键值对
     * @return
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody Map map, HttpSession session) {
        log.info("用户在尝试登录，手机号、验证码为：{}", map.toString());

        //获取用户手机号和验证码
        String phone = (String) map.get(Code.PHONE);
        String code = (String) map.get(Code.CODE);

        //从session中获取手机号和验证码，来比对
        //String codeInSession = (String) session.getAttribute(phone);
        //从缓存中获取验证码
        String codeInSession = (String) redisTemplate.opsForValue().get(phone);

        if (code.equals(codeInSession)) {
            //检查用户是否已注册，若没注册，则自动注册
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
            lqw.eq(User::getPhone, phone);
            User user = userService.getOne(lqw);
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            session.setAttribute(Code.USER, user.getId());

            //若登录成功，则从redis中删除验证码
            redisTemplate.delete(phone);
            return Result.success("验证码校验通过，请登录！");
        }

        return Result.error("验证码校验失败，登录失败");
    }

    /**
     * 发送验证码
     *
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取手机号
        String phone = user.getPhone();

        //调用阿里云发送验证码
        if (StringUtils.isNotEmpty(phone)) {
            //随机生成验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("生成验证码为：{}", code);

            //发送
//            SMSUtils.sendMessage("Reggie", "", phone, code);

            //将验证码保存到session
//            session.setAttribute(phone, code);

            //缓存到redis中
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

            return Result.success("验证码发送成功");
        }

        return Result.error("验证码发送失败");
    }


}
