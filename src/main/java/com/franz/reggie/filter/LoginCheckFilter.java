package com.franz.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.franz.reggie.common.BaseContext;
import com.franz.reggie.common.Code;
import com.franz.reggie.common.Result;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

/**
 * 使用过滤器检查用户是否已登录
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        log.info("LoginCheckFilter: 拦截到请求：{}", request.getRequestURI());

        //1.获取本次请求uri
        String requestURI = request.getRequestURI();

        //定义不需要处理的路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login",
                "/doc.html",
                "webjars/**",
                "/swagger-resources",
                "/v2/api-docs",
                "/swagger-ui.html",
                "/v3/api-docs"
        };

        //2.判断是否需要处理
        boolean match = checkUrl(requestURI, urls);

        //3.不需要处理直接放行
        if (match) {
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //4-1.判断后台员工登录状态，若已登录，同样放行[这里使用的是服务端存储的session来校验]
        if (request.getSession().getAttribute(Code.EMPLOYEE) != null) {
            log.info("员工{}已登录", request.getSession().getAttribute(Code.EMPLOYEE));

            //在过滤器中保存当前登录用户id
            Long employeeId = (Long) request.getSession().getAttribute(Code.EMPLOYEE);
            BaseContext.setCurrentId(employeeId);

            filterChain.doFilter(request, response);
            return;
        }

        //4-2.判断前端用户登录状态，若已登录，同样放行[这里使用的是服务端存储的session来校验]
        if (request.getSession().getAttribute(Code.USER) != null) {
            log.info("用户{}已登录", request.getSession().getAttribute(Code.USER));

            //在过滤器中保存当前登录用户id
            Long userId = (Long) request.getSession().getAttribute(Code.USER);
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }

        //5.未登录，则通过日志流方式写回响应数据
        log.info("用户{}未登录", request.getSession().getAttribute(Code.EMPLOYEE));
        response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
        return;
    }

    public boolean checkUrl(String url, String[] urls) {
        for (String s : urls) {
            if (PATH_MATCHER.match(s, url)) {
                return true;
            }
        }
        return false;
    }
}
