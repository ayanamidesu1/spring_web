package com.example.web_pixiv;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component
public class BaseClass {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    public BaseClass() {
        // 配置ObjectMapper日期格式
        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // objectMapper.setDateFormat(dateFormat);
        // 如果需要严格ISO 8601格式（带有时区），使用以下配置：
         objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
         objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
    }
    /**
     * 执行SQL语句
     * @param sql SQL语句
     * @param params 参数数组
     * @return 如果returnResult为true返回结果集，否则返回受影响行数
     */

    public String execute_arr(String sql, Object[] params) {
        try {

                List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, params);
                // 将结果集转换为JSON字符串并返回，并自动将时间类型转换为标准ISO 8601格式

                return objectMapper.writeValueAsString(result);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
    public  String execute(String sql, Object[] params) {
        try {

                Map<String, Object> result = jdbcTemplate.queryForMap(sql, params);
                return objectMapper.writeValueAsString(result);

        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * 插入数据并返回自增主键和受影响行数
     * @param sql 插入SQL语句
     * @param params 参数数组
     * @return JSON格式: {"count":x,"ids":[id1,id2,...]}
     */
    public String insertAndReturnKey(String sql, Object[] params) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            int count = jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                return ps;
            }, keyHolder);

            List<Long> ids = new ArrayList<>();
            if (keyHolder.getKeys() != null) {
                // 处理可能的多列主键情况
                keyHolder.getKeys().values().forEach(id -> ids.add(Long.valueOf(id.toString())));
            } else if (keyHolder.getKey() != null) {
                ids.add(keyHolder.getKey().longValue());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("ids", ids);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    /**
     * 插入数据
     * @param sql 插入SQL语句
     * @param params 参数数组
     * @return JSON格式: {"count":x}
     */
    public String insert(String sql, Object[] params) {
        try {
            int count = jdbcTemplate.update(sql, params);
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    /**
     * 更新数据
     * @param sql 更新SQL语句
     * @param params 参数数组
     * @return JSON格式: {"count":x}
     */
    public String update(String sql, Object[] params) {
        try {
            int count = jdbcTemplate.update(sql, params);
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    /**
     * 删除数据
     * @param sql 删除SQL语句
     * @param params 参数数组
     * @return JSON格式: {"count":x}
     */
    public String delete(String sql, Object[] params) {
        try {
            int count = jdbcTemplate.update(sql, params);
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    /**
     * 批量插入数据
     * @param sql 插入SQL语句
     * @param batchParams 批量参数数组
     * @return JSON格式: {"count":x,"details":[x1,x2,...]}
     */
    public String batchInsert(String sql, List<Object[]> batchParams) {
        try {
            int[] rowsArray = jdbcTemplate.batchUpdate(sql, batchParams);
            int totalCount = 0;
            for (int rows : rowsArray) {
                totalCount += rows;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("count", totalCount);
            result.put("details", rowsArray);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    /**
     * 批量更新数据
     * @param sql 更新SQL语句
     * @param batchParams 批量参数数组
     * @return JSON格式: {"count":x,"details":[x1,x2,...]}
     */
    public String batchUpdate(String sql, List<Object[]> batchParams) {
        try {
            int[] rowsArray = jdbcTemplate.batchUpdate(sql, batchParams);
            int totalCount = 0;
            for (int rows : rowsArray) {
                totalCount += rows;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("count", totalCount);
            result.put("details", rowsArray);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    /**
     * 通用请求体返回方法
     * @param result 结果集
     * @param status 状态码
     * @return 响应体
     */
    public String response(List<Map<String, Object>> result, int status) throws JsonProcessingException {
        StringBuilder response = new StringBuilder();
        response.append("{\"status\": ").append(status);
        if (result != null) {
            response.append(", \"result\": ").append(objectMapper.writeValueAsString(result));
        }
        response.append("}");
        return response.toString();
    }

    /**
     * 自动拦截所有子类的GET请求
     */
    @RequestMapping(method = RequestMethod.GET)
    public String interceptAllGetRequests() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", 404);
            response.put("msg", "不允许的请求方式");
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return "{\"status\":500,\"msg\":\"内部服务器错误\"}";
        }
    }


    /**
     * 生成错误响应
     * @param e 异常对象
     * @return JSON格式的错误信息
     */
    private String errorResponse(Exception e) {
        try {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return objectMapper.writeValueAsString(error);
        } catch (JsonProcessingException ex) {
            return "{\"error\":\"Failed to generate error response\"}";
        }
    }


}
