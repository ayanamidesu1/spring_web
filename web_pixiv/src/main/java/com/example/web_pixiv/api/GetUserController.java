package com.example.web_pixiv.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.web_pixiv.BaseClass;
import com.example.web_pixiv.config.Config;
import com.example.web_pixiv.logger.RequestLogger;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class GetUserController {

    private final BaseClass baseClass;

    public GetUserController(BaseClass baseClass) {
        this.baseClass = baseClass;
    }

    @GetMapping
    public ResponseEntity<?> getUsers() {
        try {
            RequestLogger.log(RequestLogger.Level.INFO, "Getting users");
            String sql = "SELECT * FROM users";
            //result可能为数组也可能为字
            String path=Config.DIR;
            System.out.println(path);

            Object result = baseClass.execute(sql, null);
            //判断是否为可能为数组
            System.out.println(result);
            if(result != null){
                return ResponseEntity.ok(result);
            }
            // 处理不同类型的返回结果
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected return type from execute()"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
