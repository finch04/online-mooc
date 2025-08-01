package com.tianji.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.auth.AuthClient;
import com.tianji.api.dto.auth.RoleDTO;
import com.tianji.api.dto.user.LoginFormDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.LoginUserDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.enums.UserType;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.exceptions.ForbiddenException;
import com.tianji.common.exceptions.UnauthorizedException;
import com.tianji.common.utils.AssertUtils;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.user.domain.dto.UserFormDTO;
import com.tianji.user.domain.po.User;
import com.tianji.user.domain.po.UserDetail;
import com.tianji.user.domain.query.UserPageQuery;
import com.tianji.user.domain.query.UserSimplePageQuery;
import com.tianji.user.domain.vo.UserDetailVO;
import com.tianji.user.domain.vo.UserPageVO;
import com.tianji.user.enums.UserStatus;
import com.tianji.user.mapper.UserMapper;
import com.tianji.user.service.ICodeService;
import com.tianji.user.service.IUserDetailService;
import com.tianji.user.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tianji.user.constants.UserConstants.*;
import static com.tianji.user.constants.UserErrorInfo.Msg.*;


/**
 * <p>
 * 学员用户表 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2022-06-28
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ICodeService codeService;
    @Autowired
    private AuthClient authClient;
    @Autowired
    private IUserDetailService detailService;

    @Override
    public LoginUserDTO queryUserDetail(LoginFormDTO loginDTO, boolean isStaff) {
        // 1.判断登录方式
        Integer type = loginDTO.getType();
        User user = null;
        // 2.用户名和密码登录
        if (type == 1) {
            user = loginByPw(loginDTO);
        }
        // 3.验证码登录
        if (type == 2) {
            user = loginByVerifyCode(loginDTO.getCellPhone(), loginDTO.getPassword());
        }
        // 4.微信登录  这里借用password字段存储wxUnioinid
        if (type == 3) {
            user = loginByWxLogin(loginDTO.getPassword());
        }
        // 4.错误的登录方式
        if (user == null) {
            throw new BadRequestException(ILLEGAL_LOGIN_TYPE);
        }
        // 5.判断用户类型与登录方式是否匹配
        if (isStaff ^ user.getType() != UserType.STUDENT) {
            throw new BadRequestException(isStaff ? "非管理端用户" : "非学生端用户");
        }
        // 6.封装返回
        LoginUserDTO userDTO = new LoginUserDTO();
        userDTO.setUserId(user.getId());
        userDTO.setRoleId(handleRoleId(user));
        return userDTO;
    }

    private User loginByWxLogin(String unionid) {
        // 2.根据微信unionid查询
        User user = lambdaQuery().eq(User::getWxUnionid, unionid).one();
        if (user == null) {
            throw new BadRequestException(WXUNIONID_NOT_EXISTS);
        }
        // 3.校验是否禁用
        if (user.getStatus() == UserStatus.FROZEN) {
            throw new ForbiddenException(USER_FROZEN);
        }
        return user;
    }

    @Override
    public void resetPassword(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        AssertUtils.isTrue(updateById(user), USER_ID_NOT_EXISTS);
    }

    @Override
    public UserDetailVO myInfo() {
        // 1.获取登录用户id
        Long userId = UserContext.getUser();
        if (userId == null) {
            return null;
        }
        // 2.查询用户
        UserDetail userDetail = detailService.queryById(userId);
        AssertUtils.isNotNull(userDetail, USER_ID_NOT_EXISTS);
        // 3.封装vo
        UserType type = userDetail.getType();
        // 3.1.基本信息
        UserDetailVO vo = BeanUtils.toBean(userDetail, UserDetailVO.class);
        // 3.2.详情信息
        switch (type) {
            case STAFF:
                RoleDTO roleDTO = authClient.queryRoleById(userDetail.getRoleId());
                vo.setRoleName(roleDTO == null ? "" : roleDTO.getName());
                break;
            case STUDENT:
                vo.setRoleName(STUDENT_ROLE_NAME);
                break;
            case TEACHER:
                vo.setRoleName(TEACHER_ROLE_NAME);
                break;
            default:
                break;
        }
        return vo;
    }

    @Override
    public void addUserByPhone(User user, String code) {
        // 1.验证码校验
        codeService.verifyCode(user.getCellPhone(), code);
        // 2.判断手机号是否存在
        Integer count = lambdaQuery().eq(User::getCellPhone, user.getCellPhone()).count();
        if (count > 0) {
            throw new BadRequestException(PHONE_ALREADY_EXISTS);
        }
        // 3.加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 4.新增
        user.setUsername(user.getCellPhone());
        save(user);
    }

    @Override
    public void updatePasswordByPhone(String cellPhone, String code, String password) {
        // 1.验证码校验
        codeService.verifyCode(cellPhone, code);
        // 2.查询用户
        User oldUser = lambdaQuery().eq(User::getCellPhone, cellPhone).one();
        if (oldUser == null) {
            // 手机号不存在
            throw new BadRequestException(PHONE_NOT_EXISTS);
        }
        // 2.修改密码
        User user = new User();
        user.setId(oldUser.getId());
        user.setPassword(passwordEncoder.encode(password));
        updateById(user);
    }

    public void updatePhoneById(Long id, String cellPhone) {
        // 1.1.判断是否需要修改手机号
        if (StringUtils.isNotBlank(cellPhone)) {
            // 1.2.需要修改，封装数据
            User user = new User();
            user.setId(id);
            user.setUsername(cellPhone);
            user.setCellPhone(cellPhone);
            // 1.3.修改
            updateById(user);
        }
    }

    @Override
    @Transactional
    public Long saveUser(UserDTO userDTO) {
        UserType type = UserType.of(userDTO.getType());
        // 1.保存用户基本信息
        User user = new User();
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setCellPhone(userDTO.getCellPhone());
        user.setUsername(userDTO.getCellPhone());
        user.setType(type);
        save(user);
        // 2.新增详情
        UserDetail detail = BeanUtils.toBean(userDTO, UserDetail.class);
        detail.setId(user.getId());
        detail.setType(type);
        if(type == UserType.TEACHER){
            detail.setRoleId(TEACHER_ROLE_ID);
        }else{
            if (userDTO.getRoleId() == null) {
                throw new BadRequestException("员工角色信息不能为空");
            }
        }
        detailService.save(detail);
        return user.getId();
    }

    @Override
    @Transactional
    public void updateUser(UserDTO userDTO) {
        // 1.如果传递了手机号，则修改手机号
        String cellphone = userDTO.getCellPhone();
        if(StringUtils.isNotBlank(cellphone)){
            User user = new User();
            user.setId(userDTO.getId());
            user.setCellPhone(cellphone);
            user.setUsername(cellphone);
            updateById(user);
        }
        // 2.修改详情
        UserDetail detail = BeanUtils.toBean(userDTO, UserDetail.class);
        detail.setType(null);
        detailService.updateById(detail);
    }

    @Override
    @Transactional
    public void updateUserWithPassword(UserFormDTO userDTO) {
        // 1.尝试更新密码
        String pw = userDTO.getPassword();
        String oldPw = userDTO.getOldPassword();
        if(StringUtils.isNotBlank(pw) && StringUtils.isNotBlank(pw)) {
            Long userId = UserContext.getUser();
            // 1.1.查询用户
            User user = getById(userId);
            // 1.2.校验
            if (user == null) {
                throw new BadRequestException(USER_ID_NOT_EXISTS);
            }
            // 1.3.校验密码
            if (!passwordEncoder.matches(oldPw, user.getPassword())) {
                // 密码不一致
                throw new BadRequestException(INVALID_OLD_PASSWORD);
            }
//            // 1.4.修改密码
//            user = new User();
//            user.setId(userId);
//            user.setPassword(passwordEncoder.encode(pw));
//            updateById(user);
            this.lambdaUpdate()
                    .eq(User::getId, userId)
                    .set(User::getPassword, passwordEncoder.encode(pw))
                    .update();
        }
        // 2.更新用户详情
        UserDetail detail = BeanUtils.toBean(userDTO, UserDetail.class);
        LambdaUpdateWrapper<UserDetail> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserDetail::getId, detail.getId());

        // 依次检查每个字段是否为 null，如果不为 null 则添加到更新条件中
        if (detail.getName() != null) {
            updateWrapper.set(UserDetail::getName, detail.getName());
        }
        if (detail.getGender() != null) {
            updateWrapper.set(UserDetail::getGender, detail.getGender());
        }
        if (detail.getIcon() != null) {
            updateWrapper.set(UserDetail::getIcon, detail.getIcon());
        }
        if (detail.getEmail() != null) {
            updateWrapper.set(UserDetail::getEmail, detail.getEmail());
        }
        if (detail.getQq() != null) {
            updateWrapper.set(UserDetail::getQq, detail.getQq());
        }
        if (detail.getBirthday() != null) {
            updateWrapper.set(UserDetail::getBirthday, detail.getBirthday());
        }
        if (detail.getJob() != null) {
            updateWrapper.set(UserDetail::getJob, detail.getJob());
        }
        if (detail.getProvince() != null) {
            updateWrapper.set(UserDetail::getProvince, detail.getProvince());
        }
        if (detail.getCity() != null) {
            updateWrapper.set(UserDetail::getCity, detail.getCity());
        }
        if (detail.getDistrict() != null) {
            updateWrapper.set(UserDetail::getDistrict, detail.getDistrict());
        }
        if (detail.getIntro() != null) {
            updateWrapper.set(UserDetail::getIntro, detail.getIntro());
        }
        if (detail.getPhoto() != null) {
            updateWrapper.set(UserDetail::getPhoto, detail.getPhoto());
        }
        updateWrapper.set(UserDetail::getUpdateTime, new Date());
        // 执行更新操作
         detailService.update(updateWrapper);

    }

    public User loginByPw(LoginFormDTO loginDTO) {
        // 1.数据校验
        String username = loginDTO.getUsername();
        String cellPhone = loginDTO.getCellPhone();
        if (StrUtil.isBlank(username) && StrUtil.isBlank(cellPhone)) {
            throw new BadRequestException(INVALID_UN);
        }
        // 2.根据用户名或手机号查询
        User user = lambdaQuery()
                .eq(StrUtil.isNotBlank(username), User::getUsername, username)
                .eq(StrUtil.isNotBlank(cellPhone), User::getCellPhone, cellPhone)
                .one();
        AssertUtils.isNotNull(user, INVALID_UN_OR_PW);
        // 3.校验是否禁用
        if (user.getStatus() == UserStatus.FROZEN) {
            throw new ForbiddenException(USER_FROZEN);
        }
        // 4.校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BadRequestException(INVALID_UN_OR_PW);
        }

        return user;
    }

    private Long handleRoleId(User user) {
        Long roleId = 0L;
        switch (user.getType()) {
            case STUDENT:
                roleId = STUDENT_ROLE_ID;
                break;
            case TEACHER:
                roleId = TEACHER_ROLE_ID;
                break;
            case STAFF:
                UserDetail detail = detailService.getById(user.getId());
                roleId = detail.getRoleId();
                break;
        }
        return roleId;
    }

    public User loginByVerifyCode(String phone, String code) {
        // 1.校验验证码
        codeService.verifyCode(phone, code);
        // 2.根据手机号查询
        User user = lambdaQuery().eq(User::getCellPhone, phone).one();
        if (user == null) {
            throw new BadRequestException(PHONE_NOT_EXISTS);
        }
        // 3.校验是否禁用
        if (user.getStatus() == UserStatus.FROZEN) {
            throw new ForbiddenException(USER_FROZEN);
        }
        return user;
    }

    @Override
    public Boolean checkCellPhone(String cellPhone) {
        return this.lambdaQuery()
                .eq(User::getCellPhone, cellPhone)
                // .in(User::getType, UserType.STAFF, UserType.TEACHER)
                .count() <= 0;
    }

    @Override
    public PageDTO<UserPageVO> queryUserPage(UserSimplePageQuery pageQuery) {
        // 执行分页查询
        Page<UserDetail> page = detailService.lambdaQuery()
                .eq(pageQuery.getType() != null, UserDetail::getType, pageQuery.getType())
                .eq(pageQuery.getGender() != null, UserDetail::getGender, pageQuery.getGender())
                .like(StrUtil.isNotBlank(pageQuery.getName()), UserDetail::getName, pageQuery.getName())
                .page(pageQuery.toMpPageDefaultSortByCreateTimeDesc());

        // 转换为VO列表
        List<UserPageVO> list = page.getRecords().stream()
                .map(user -> {
                    UserPageVO vo = BeanUtils.toBean(user, UserPageVO.class);
                    // 如果UserPageVO的字段与UserDetail完全一致，这行即可
                    // 如果有特殊字段需要额外处理，可以在这里添加
                    return vo;
                })
                .filter(Objects::nonNull) // 过滤可能的null值
                .collect(Collectors.toList());

        return PageDTO.of(page, list);
    }

    @Override
    public Boolean checkPasswd(String password) {
        Long user = UserContext.getUser();
        User userInfo = getById(user);
        return passwordEncoder.matches(password, userInfo.getPassword());
    }
}
