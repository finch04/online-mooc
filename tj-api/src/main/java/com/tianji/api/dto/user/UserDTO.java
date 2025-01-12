package com.tianji.api.dto.user;

import com.tianji.common.constants.RegexConstants;
import com.tianji.common.validate.annotations.EnumValid;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
@Schema(description = "用户详情")
public class UserDTO {
    @Schema(description = "用户id", example = "1")
    private Long id;
    @Schema(description = "手机", example = "13890011009")
    @Pattern(regexp = RegexConstants.PHONE_PATTERN, message = "手机号格式错误")
    private String cellPhone;
    @Schema(description = "用户名称/昵称", example = "李四")
    private String name;
    @Schema(description = "用户类型，1-其他员工,2-普通学员，3-老师", example = "2")
    @EnumValid(enumeration = {1, 2, 3}, message = "用户类型错误")
    @NotNull
    private Integer type;
    @Schema(description = "角色id，老师和学生不用填", example = "5")
    private Long roleId;
    @Schema(description = "头像", example = "default-user-icon.jpg")
    private String icon;
    @Schema(description = "岗位", example = "讲师")
    private String job;
    @Schema(description = "个人介绍", example = "黑马高级Java讲师")
    private String intro;
    @Schema(description = "形象照地址", example = "default-teacher-photo.jpg")
    private String photo;
    @Schema(description = "用户名", example = "13800010004")
    private String username;
    @Schema(description = "邮箱")
    @Email
    private String email;
    @Schema(description = "QQ号码")
    private String qq;
    @Schema(description = "省")
    private String province;
    @Schema(description = "市")
    private String city;
    @Schema(description = "区")
    private String district;
    @Schema(description = "性别：0-男性，1-女性", example = "0")
    @EnumValid(enumeration = {0, 1}, message = "性别格式不正确")
    private Integer gender;
}
