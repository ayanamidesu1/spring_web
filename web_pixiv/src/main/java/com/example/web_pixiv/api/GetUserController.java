package com.example.web_pixiv.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.web_pixiv.BaseClass;
import com.example.web_pixiv.config.Config;
import com.example.web_pixiv.logger.RequestLogger;
import com.example.web_pixiv.core.ResponseBase;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/user")
public class GetUserController {

    private final BaseClass baseClass;

    public GetUserController(BaseClass baseClass) {
        this.baseClass = baseClass;
    }

    @PostMapping
    public ResponseEntity<?> getUsers(HttpServletRequest request) {
        try {
            RequestLogger.log(RequestLogger.Level.INFO, "Getting users");
            //获取request中的user
            Object user =  ResponseBase.getRequestValue(request, "user");
            System.out.printf("user: %s\n", user);
            Boolean is_login = (Boolean) ResponseBase.getRequestValue(request, "is_login");
            System.out.printf("is_login: %s\n", is_login);

            String sql = "SELECT * FROM users";
            //result可能为数组也可能为字
            String path=Config.DIR;
            System.out.println(path);

            Object result = baseClass.execute(sql, null);
            //判断是否为可能为数组
            //System.out.println(result);
            if(result != null){
                return ResponseEntity.ok(Map.of("code",200,"msg","success","data",result));
            }
            // 处理不同类型的返回结果
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected return type from execute()"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            RequestLogger.log(RequestLogger.Level.ERROR, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
