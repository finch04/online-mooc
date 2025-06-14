package com.tianji.data.controller;/**
 * @author fsq
 * @date 2025/5/18 19:02
 */

import com.tianji.api.dto.data.TodoDataVO;
import com.tianji.data.service.TodoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: fsq
 * @Date: 2025/5/18 19:02
 * @Version: 1.0
 */
@RestController
@Api(tags = "工作台待办数据相关接口")
@RequestMapping("/data/todo")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @GetMapping("")
    @ApiOperation("待办事项数据获取")
    public TodoDataVO getTodoData() {
        return todoService.get();
    }


}