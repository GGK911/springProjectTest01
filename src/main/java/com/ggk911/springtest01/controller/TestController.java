package com.ggk911.springtest01.controller;

import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.URLUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author TangHaoKai
 * @version V1.0 2023-11-21 13:43
 **/
@RestController
public class TestController {

    /**
     * 测试启动
     *
     * @return hello world
     */
    @RequestMapping("/helloworld")
    public String test() {
        return "hello world";
    }

    /**
     * 参数转发带参数
     *
     * @param response 响应
     */
    @RequestMapping("/redirect")
    public void redirectTest(HttpServletRequest request, HttpServletResponse response) {
        try {
            Thread.sleep(3000);
            response.sendRedirect("https://www.baidu.com/s?wd=" + URLUtil.encode("搜索"));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
