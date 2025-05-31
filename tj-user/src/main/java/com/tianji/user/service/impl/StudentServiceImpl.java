package com.tianji.user.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.api.client.trade.TradeClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.enums.UserType;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.RandomUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.user.constants.UserConstants;
import com.tianji.user.domain.dto.StudentFormDTO;
import com.tianji.user.domain.dto.StudentUpdateDTO;
import com.tianji.user.domain.dto.StudentUpdatePasswordDTO;
import com.tianji.user.domain.po.User;
import com.tianji.user.domain.po.UserDetail;
import com.tianji.user.domain.query.UserPageQuery;
import com.tianji.user.domain.vo.StudentPageVo;
import com.tianji.user.service.IStudentService;
import com.tianji.user.service.IUserDetailService;
import com.tianji.user.service.IUserService;
import com.tianji.user.utils.NameUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tianji.user.constants.UserConstants.DEFAULT_PASSWORD;

/**
 * <p>
 * 学员详情表 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2022-07-12
 */
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements IStudentService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final IUserService userService;
    private final IUserDetailService detailService;
    private final TradeClient tradeClient;
    private final CodeServiceImpl codeService;

    @Override
    @Transactional
    public void saveStudent(StudentFormDTO studentFormDTO) {
        // 1.新增用户账号
        User user = new User();
        user.setCellPhone(studentFormDTO.getCellPhone());
        user.setPassword(studentFormDTO.getPassword());
        user.setType(UserType.STUDENT);
        //这里已经判断了手机号是否已经存在
        userService.addUserByPhone(user, studentFormDTO.getCode());

        // 2.新增学员详情
        UserDetail student = new UserDetail();
        student.setId(user.getId());
        student.setName(NameUtils.getUserName());
        student.setRoleId(UserConstants.STUDENT_ROLE_ID);
        detailService.save(student);
    }

    @Override
    public void updateMyPassword(StudentFormDTO studentFormDTO) {
        userService.updatePasswordByPhone(
                studentFormDTO.getCellPhone(), studentFormDTO.getCode(), studentFormDTO.getPassword()
        );
    }


    @Override
    public PageDTO<StudentPageVo> queryStudentPage(UserPageQuery query) {
        // 1.分页条件
        Page<UserDetail> page  =  detailService.queryUserDetailByPage(query, UserType.STUDENT);
        List<UserDetail> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }

        // 2.查询购买的课程数量
        List<Long> stuIds = records.stream().map(UserDetail::getId).collect(Collectors.toList());
        Map<Long, Integer> numMap = tradeClient.countEnrollCourseOfStudent(stuIds);

        // 3.处理vo
        List<StudentPageVo> list = new ArrayList<>(records.size());
        for (UserDetail r : records) {
            StudentPageVo v = BeanUtils.toBean(r, StudentPageVo.class);
            list.add(v);
            v.setCourseAmount(numMap.get(r.getId()));
        }
        return new PageDTO<>(page.getTotal(), page.getPages(), list);
    }


    @Override
    public void updateStudent(StudentUpdateDTO studentUpdateDTO) {
        if(!studentUpdateDTO.getId().equals(UserContext.getUser())){
            throw new BizIllegalException("只能修改自己的信息！");
        }
        UserDTO dto = BeanUtils.copyProperties(studentUpdateDTO, UserDTO.class);
        userService.updateUser(dto);
    }

    @Override
    public void updateBindPhone(String cellPhone, String code) {
        codeService.verifyCode(cellPhone, code);
        if(!userService.checkCellPhone(cellPhone)){
            throw new BizIllegalException("手机号已绑定账号！");
        }
        Long id = UserContext.getUser();
        User user = new User();
        user.setId(id);
        user.setCellPhone(cellPhone);
        userService.updateById(user);
    }

    @Override
    public void updatePassword(StudentUpdatePasswordDTO dto) {
        if(!dto.getId().equals(UserContext.getUser())){
            throw new BizIllegalException("只能修改自己的信息！");
        }
        User user = userService.getById(dto.getId());
        if(!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())){
            throw new BizIllegalException("原密码错误！");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userService.updateById(user);
    }
}
