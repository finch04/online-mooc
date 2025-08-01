package com.tianji.data.service;


import com.tianji.api.dto.data.TodoDataVO;
import com.tianji.data.model.dto.Top10DataSetDTO;
import com.tianji.data.model.vo.Top10DataVO;

/**
 * @author wusongsong
 * @since 2022/10/10 19:39
 **/
public interface TodoService {

    /**
     * 待办事项
     * @return
     */
    TodoDataVO get();

    /**
     * 更新待办事项
     * @param vo
     */
    void updateTodoData(TodoDataVO vo);
}