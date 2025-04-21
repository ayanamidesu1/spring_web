package com.example.web_pixiv.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ResponseBase {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从Request中获取所有参数（包括Header和Parameter）
     */
    public static Map<String, Object> getAllRequestValues(HttpServletRequest request) {
        Map<String, Object> values = new HashMap<>();

        // 获取URL参数
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String paramName = params.nextElement();
            values.put("param_" + paramName, request.getParameter(paramName));
        }

        // 获取Header
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String headerName = headers.nextElement();
            values.put("header_" + headerName, request.getHeader(headerName));
        }

        // 获取Attribute
        Enumeration<String> attributes = request.getAttributeNames();
        while (attributes.hasMoreElements()) {
            String attrName = attributes.nextElement();
            values.put("attr_" + attrName, request.getAttribute(attrName));
        }

        return values;
    }

    /**
     * 通过键获取Request中的值
     */
    public static Object getRequestValue(HttpServletRequest request, String key) {
        // 检查顺序：Parameter -> Header -> Attribute
        String value = request.getParameter(key);
        if (value != null) return value;

        value = request.getHeader(key);
        if (value != null) return value;

        return request.getAttribute(key);
    }

    /**
     * 构建标准化JSON响应
     */
    public static ResponseEntity<String> buildResponse(
            HttpStatus status,
            String message,
            Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", status.value());
        response.put("message", message);
        response.put("data", data);

        try {
            return ResponseEntity
                    .status(status)
                    .header("Content-Type", "application/json")
                    .body(objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"JSON processing error\"}");
        }
    }

    /**
     * 从Request构建错误响应（自动提取错误信息）
     */
    public static ResponseEntity<String> buildErrorResponse(
            HttpServletRequest request,
            HttpStatus defaultStatus) {
        HttpStatus status = defaultStatus;
        String message = (String) request.getAttribute("error_message");
        Object errorData = request.getAttribute("error_data");

        if (request.getAttribute("javax.servlet.error.status_code") != null) {
            int code = (int) request.getAttribute("javax.servlet.error.status_code");
            status = HttpStatus.valueOf(code);
        }

        return buildResponse(status, message != null ? message : status.getReasonPhrase(), errorData);
    }
}