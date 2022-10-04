package com.colin.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author colin
 * @create 2022-10-02 17:28
 */
@RestController
@Slf4j
public class HelloController {

    @Value("${server.port}")
    private String port;

    @RequestMapping("/hello")
    public String hello(){

        // 稳定版本输出内容
        /*return "hello gray controller，port = " + port;*/

        // 灰度版本输出内容
        return "hello gray controller，port = " + port;
    }

}
