package com.tianji.aigc.tools;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.config.ToolResultHolder;
import com.tianji.aigc.constants.Constant;
import org.springframework.ai.chat.model.ToolContext;

public class BaseTools {


    /**
     * 存结果到 ToolResultHolder
     *
     * @param toolContext 工具上下文
     * @param field       结果字段
     * @param result      需要保存的结果对象
     * @return 原始对象
     */
    public <T> T saveResult(ToolContext toolContext, String field, T result) {
        if (null == result) {
            return null;
        }
        var requestId = Convert.toStr(toolContext.getContext().get(Constant.REQUEST_ID));
        ToolResultHolder.put(requestId, field, result);
        return result;
    }

    /**
     * 存结果到 ToolResultHolder，默认字段为：首字母小写类名
     *
     * @param toolContext 工具上下文
     * @param result      需要保存的结果对象
     * @return 原始对象
     */
    public <T> T saveResult(ToolContext toolContext, T result) {
        if (null == result) {
            return null;
        }
        var field = StrUtil.lowerFirst(result.getClass().getSimpleName());
        return this.saveResult(toolContext, field, result);
    }

}
