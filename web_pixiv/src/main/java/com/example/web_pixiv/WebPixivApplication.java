package com.example.web_pixiv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = {
        "org.springframework.boot.autoconfigure.web.servlet.CorsAutoConfiguration"
})

public class WebPixivApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebPixivApplication.class, args);
    }

}
