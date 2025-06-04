package com.tianji.user.controller;


import com.tianji.common.domain.dto.PageDTO;
import com.tianji.user.domain.dto.StudentFormDTO;
import com.tianji.user.domain.dto.StudentUpdateDTO;
import com.tianji.user.domain.dto.StudentUpdatePasswordDTO;
import com.tianji.user.domain.query.UserPageQuery;
import com.tianji.user.domain.vo.StudentPageVO;
import com.tianji.user.service.ICodeService;
import com.tianji.user.service.IStudentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 学员详情表 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2022-07-12
 */
@RestController
@RequestMapping("/students")
@Api(tags = "学员管理接口")
public class StudentController {

    @Autowired
    private IStudentService studentService;
    @Autowired
    private ICodeService codeService;

    @ApiOperation("分页查询学生信息")
    @GetMapping("/page")
    public PageDTO<StudentPageVO> queryStudentPage(UserPageQuery pageQuery){
        return studentService.queryStudentPage(pageQuery);
    }


    @ApiOperation("学员注册")
    @PostMapping("/register")
    public void registerStudent(@RequestBody StudentFormDTO studentFormDTO) {
        studentService.saveStudent(studentFormDTO);
    }

    @ApiOperation("学员找回密码")
    @PutMapping("/password")
    public void updateMyPassword(@RequestBody StudentFormDTO studentFormDTO) {
        studentService.updateMyPassword(studentFormDTO);
    }

    @ApiOperation("学员更新个人信息")
    @PutMapping("")
    public void updateStudent(@RequestBody @Valid StudentUpdateDTO studentUpdateDTO){
        studentService.updateStudent(studentUpdateDTO);
    }

    @ApiOperation("解绑手机号 发送验证码")
    @PostMapping("/sendSms")
    public void sendVerifyCode(@RequestParam String cellPhone){
        codeService.sendVerifyCode(cellPhone);
    }

    @ApiOperation("更新绑定手机号")
    @PostMapping("/updateBindPhone")
    public void updateBindPhone(@RequestParam String cellPhone,@RequestParam String code){
        studentService.updateBindPhone(cellPhone,code);
    }

    @ApiOperation("学员修改密码")
    @PutMapping("/updatePassword")
    public void updatePassword(@RequestBody StudentUpdatePasswordDTO dto) {
        studentService.updatePassword(dto);
    }

}
