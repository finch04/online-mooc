package com.tianji.trade.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 支付渠道vo对象
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-26
 */
@Schema(description = "支付渠道信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayChannelVO {

    @Schema(description = "支付渠道id")
    private Long id;
    @Schema(description = "支付渠道名称")
    private String name;
    @Schema(description = "支付渠道编码，唯一标示")
    private String channelCode;
    @Schema(description = "渠道优先级，数字越小优先级越高")
    private Integer channelPriority;
    @Schema(description = "渠道图标")
    private String channelIcon;
}