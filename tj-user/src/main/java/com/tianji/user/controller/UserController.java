package com.tianji.user.controller;

import com.tianji.api.dto.user.LoginFormDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.LoginUserDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.user.constants.UserErrorInfo;
import com.tianji.user.domain.dto.UserFormDTO;
import com.tianji.user.domain.po.User;
import com.tianji.user.domain.po.UserDetail;
import com.tianji.user.domain.vo.UserDetailVO;
import com.tianji.user.enums.UserStatus;
import com.tianji.user.service.IUserDetailService;
import com.tianji.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理接口
 */

@RestController
@RequestMapping("users")
@Tag(name = "用户管理接口")
public class UserController {

    @Autowired
    private IUserService userService;
    @Autowired
    private IUserDetailService detailService;

    @Operation(summary = "新增用户，一般是员工或教师")
    @PostMapping
    public Long saveUser(@Valid @RequestBody UserDTO userDTO){
        userDTO.setId(null);
        return userService.saveUser(userDTO);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public void updateUser(@RequestBody UserDTO userDTO){
        userService.updateUser(userDTO);
    }

    @Operation(summary = "更新当前登录用户信息，可修改密码")
    @PutMapping
    public void updateCurrentUser(@Valid @RequestBody UserFormDTO userDTO){
        userService.updateUserWithPassword(userDTO);
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/password/default")
    public void resetPassword(
            @Parameter(description = "要重置的用户的id", example = "1") @PathVariable("id") Long userId) {
        userService.resetPassword(userId);
    }

    @Operation(summary = "修改用户状态, status=0为禁用，status=1为正常")
    @PutMapping("/{id}/status/{status}")
    public void updateUserStatus(
            @Parameter(description = "要重置的用户的id", example = "1") @PathVariable("id") Long userId,
            @Parameter(description = "状态", example = "1") @PathVariable("status") Integer status
    ) {
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.of(status));
        userService.updateById(user);
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping(value = "/me")
    public UserDetailVO me() {
        return userService.myInfo();
    }

    @Operation(summary = "根据id查询用户信息")
    @GetMapping("/{id}")
    public UserDTO queryUserById(
            @Parameter(description = "用户id") @PathVariable("id") Long id) {
        UserDetail userDetail = detailService.queryById(id);
        return BeanUtils.copyBean(userDetail, UserDTO.class, (d, u) -> u.setType(d.getType().getValue()));
    }

    /**
     * 登录结构
     * @param loginDTO 登录表单
     * @param isStaff 是否是后台登录
     * @return 登录用户信息
     */
    @Operation(hidden = true)
    @PostMapping("/detail/{isStaff}")
    public LoginUserDTO queryUserDetail(
            @Valid @RequestBody LoginFormDTO loginDTO, @PathVariable("isStaff") boolean isStaff) {
        return userService.queryUserDetail(loginDTO, isStaff);
    }

    /**
     * 根据id批量查询用户信息
     *
     * @param ids 用户id集合
     * @return 用户集合
     */
    @Operation(hidden = true)
    @GetMapping("/list")
    public List<UserDTO> queryUserByIds(
            @Parameter(description = "用户id的列表") @RequestParam("ids") List<Long> ids) {
        if(CollUtils.isEmpty(ids)){
            return CollUtils.emptyList();
        }
        // 1.查询列表
        List<UserDetail> list = detailService.queryByIds(ids);
        // 2.转换
        return BeanUtils.copyList(list, UserDTO.class, (d, u) -> u.setType(d.getType().getValue()));
    }

    /**
     * 查询用户类型
     *
     * @param id 用户id
     * @return 用户类型，0-普通学员，1-老师，2-其他员工
     */
    @Operation(hidden = true)
    @GetMapping("/{id}/type")
    public Integer queryUserType(@PathVariable("id") Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BadRequestException(UserErrorInfo.Msg.USER_ID_NOT_EXISTS);
        }
        return user.getType().getValue();
    }

    @Operation(hidden = true)
    @GetMapping("/ids")
    public Long exchangeUserIdWithPhone(@RequestParam("phone") String phone) {
        User user = userService
                .lambdaQuery().eq(User::getCellPhone, phone).one();
        if (user == null) {
            throw new BadRequestException(UserErrorInfo.Msg.USER_ID_NOT_EXISTS);
        }
        return user.getId();
    }

    @Operation(summary = "检查用户手机号是否存在")
    @GetMapping("checkCellphone")
    public Boolean checkCellPhone(@RequestParam("cellphone") String cellPhone){
        return userService.lambdaQuery()
                .eq(User::getCellPhone, cellPhone)
                // .in(User::getType, UserType.STAFF, UserType.TEACHER)
                .count() <= 0;
    }
}
