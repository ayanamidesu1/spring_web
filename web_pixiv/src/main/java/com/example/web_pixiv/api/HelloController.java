package com.example.web_pixiv.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {  // 类名通常使用大写字母开头
    @GetMapping("/hello")
    public String sayHello() {
        return "hello_world";
    }
}
