package com.tianji.pay.third.wx;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tianji.common.exceptions.CommonException;
import com.tianji.common.utils.JsonUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.pay.sdk.constants.PayConstants;
import com.tianji.pay.third.IPayService;
import com.tianji.pay.third.model.PayStatusResponse;
import com.tianji.pay.third.model.PrepayResponse;
import com.tianji.pay.third.model.RefundResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service(PayConstants.WX_CHANNEL_CODE)
@RequiredArgsConstructor
public class WxPayService implements IPayService {

    private final WxPayClient wxPayClient;

    @Override
    public PrepayResponse createPrepayOrder(String title, String orderNo, Integer amount) {
        // 1.请求地址
        String requestPath = "https://api.mch.weixin.qq.com/v3/pay/transactions/native";
        // 2.请求参数
        ObjectNode baseParam = wxPayClient.baseParam(true, true, false)
                .put("out_trade_no", orderNo)
                .put("description", title);
        baseParam.putObject("amount").put("total", amount);

        // 3.发送请求
        String responseJson = wxPayClient.doPostJson(requestPath, baseParam);

        // 4.解析
        JSONObject result = JsonUtils.parseObj(responseJson);

        String codeUrl = result.getStr("code_url");
        if (codeUrl != null) {
            // 4.1.响应正常，返回支付链接
            return PrepayResponse.builder()
                    .success(true)
                    .payUrl(codeUrl)
                    .build();
        } else {
            // 4.2.响应异常，返回异常信息
            return PrepayResponse.builder()
                    .success(false)
                    .code(result.getStr("code"))
                    .msg(result.getStr("message"))
                    .detail(result.getStr("detail"))
                    .build();
        }
    }

    @Override
    public PayStatusResponse queryPayOrderStatus(String payOrderNo) {
        // 1.请求地址
        String requestPath = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/" + payOrderNo;
        // 2.发送请求
        String responseJson = wxPayClient.doGetJson(requestPath, true);
        System.out.println("responseJson = " + responseJson);
        // 3.解析响应
        JSONObject result = JsonUtils.parseObj(responseJson);
        String code = result.getStr("code");
        String message = result.getStr("message");
        LocalDateTime successTime = result.getLocalDateTime("success_time", LocalDateTime.now());
        // 3.1.请求异常
        if(StringUtils.isNotBlank(code)){
            return PayStatusResponse.builder().success(false).code(code).msg(message).build();
        }
        // 3.2.请求成功
        return PayStatusResponse.builder()
                .success(true)
                .payOrderNo(payOrderNo)
                .payStatus(parsePayStatus(result.getStr("trade_state")))
                .msg(result.getStr("trade_state_desc"))
                .totalAmount(result.getJSONObject("amount").getInt("total"))
                .successTime(successTime)
                .build()
                ;
    }

    @Override
    public RefundResponse refundOrder(String payOrderNo, String refundOrderNo, Integer refundAmount, Integer totalAmount) {
        // 1.请求地址
        String requestPath = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";
        // 2.准备请求参数
        ObjectNode baseParam = wxPayClient.baseParam(false, false, true)
                .put("out_trade_no", payOrderNo)
                .put("out_refund_no", refundOrderNo);
        baseParam.putObject("amount")
                .put("refund", refundAmount)
                .put("total", totalAmount)
                .put("currency", "CNY")
        ;
        // 3.发送请求
        String responseJson = wxPayClient.doPostJson(requestPath, baseParam);
        // 4.解析
        JSONObject result = JsonUtils.parseObj(responseJson);
        String code = result.getStr("code");
        String message = result.getStr("message");
        // 4.1.请求异常
        if(StringUtils.isNotBlank(code)){
            return RefundResponse.builder().success(false).code(code).msg(message).build();
        }
        // 4.2.请求成功
        JSONObject amount = result.getJSONObject("amount");
        String status = result.getStr("status");
        return RefundResponse.builder()
                .success(true)
                .channel(result.getStr("channel"))
                .status(parseRefundStatus(status))
                .amount(amount == null ? 0 : amount.getInt("refund"))
                .build();
    }

    @Override
    public RefundResponse queryRefundStatus(String orderNo, String refundOrderNo) {
        // 1.请求地址
        String requestPath = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds/" + refundOrderNo;
        // 2.发送请求
        String responseJson = wxPayClient.doGetJson(requestPath, false);
        // 3.解析响应
        JSONObject result = JsonUtils.parseObj(responseJson);
        String code = result.getStr("code");
        String message = result.getStr("message");
        // 3.1.请求异常
        if(StringUtils.isNotBlank(code)){
            return RefundResponse.builder().success(false).code(code).msg(message).build();
        }
        // 3.2.请求成功
        String status = result.getStr("status");
        JSONObject amount = result.getJSONObject("amount");
        return RefundResponse.builder()
                .success(true)
                .channel(result.getStr("channel"))
                .status(parseRefundStatus(status))
                .amount(amount == null ? 0 : amount.getInt("refund"))
                .build();
    }

    private Integer parseRefundStatus(String status) {
        switch (status) {
            case "PROCESSING":
                return 1;
            case "SUCCESS":
                return 2;
            default:
                return 3;
        }
    }

    private Integer parsePayStatus(String tradeState) {
        switch (tradeState) {
            case "REFUND":
            case "CLOSED":
            case "REVOKED":
                return 2;
            case "SUCCESS":
                return 3;
            default:
                return 1;
        }
    }



    /**
     * 下载微信商户订单账单
     * @param billType 账单类型，如 ALL、SUCCESS、REFUND 等
     * @param billDate 账单日期，格式为 yyyy-MM-dd
     * @return 下载的账单文件内容（字节数组形式）
     */
    public byte[] downloadMerchantBill(String billType, String billDate) {
        // 1. 构建申请账单的请求URL
        String requestPath = "https://api.mch.weixin.qq.com/v3/bill/tradebill";

        // 2. 准备请求参数
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("bill_type", billType));
        params.add(new BasicNameValuePair("bill_date", billDate));

        // 3. 发送申请账单请求
        String responseJson = wxPayClient.doGetJson(requestPath, true, params.toArray(new NameValuePair[0]));

        // 4. 解析响应，获取下载URL
        JSONObject result = JsonUtils.parseObj(responseJson);
        String code = result.getStr("code");
        String message = result.getStr("message");

        // 4.1. 请求异常处理
        if (StringUtils.isNotBlank(code)) {
            throw new CommonException("申请账单失败，错误码：" + code + "，错误信息：" + message);
        }

        // 4.2. 获取下载URL
        String downloadUrl = result.getStr("download_url");
        if (StringUtils.isBlank(downloadUrl)) {
            throw new CommonException("申请账单成功，但未获取到下载URL");
        }

        // 5. 下载账单文件
        try {
            // 5.1. 构建HTTP请求
            HttpGet httpGet = new HttpGet(downloadUrl);
            httpGet.addHeader("Accept", "application/json");

            // 5.2. 执行请求并获取响应
            String downloadResponse = wxPayClient.doGetJson(downloadUrl, false);

            // 5.3. 这里假设响应是二进制数据，直接返回字节数组
            return downloadResponse.getBytes();
        } catch (Exception e) {
            log.error("下载微信商户账单异常，下载URL：{}", downloadUrl, e);
            throw new CommonException("下载微信商户账单异常", e);
        }
    }
}
