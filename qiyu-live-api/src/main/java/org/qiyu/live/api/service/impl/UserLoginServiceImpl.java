package org.qiyu.live.api.service.impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.account.interfaces.rpc.IAccountTokenRPC;
import org.qiyu.live.api.service.IUserLoginService;
import org.qiyu.live.api.vo.UserLoginVO;
import org.qiyu.live.common.interfaces.ConvertBeanUtils;
import org.qiyu.live.common.interfaces.vo.WebResponseVO;
import org.qiyu.live.msg.dto.MsgCheckDTO;
import org.qiyu.live.msg.enums.MsgSendResultEnum;
import org.qiyu.live.msg.interfaces.ISmsRpc;
import org.qiyu.live.user.dto.UserLoginDTO;
import org.qiyu.live.user.interfaces.rpc.IUserPhoneRPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * @Author idea
 * @Date: Created in 10:51 2023/6/15
 * @Description
 */
@Service
public class UserLoginServiceImpl implements IUserLoginService {

    private static String PHONE_REG = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";
    private static final Logger LOGGER = LoggerFactory.getLogger(UserLoginServiceImpl.class);
    @DubboReference
    private ISmsRpc smsRpc;
    @DubboReference
    private IUserPhoneRPC userPhoneRPC;
    @DubboReference
    private IAccountTokenRPC accountTokenRPC;

    @Override
    public WebResponseVO sendLoginCode(String phone) {

        // 参数校验
        if (StringUtils.isEmpty(phone)) {
            return WebResponseVO.errorParam("手机号不能为空");
        }
        if (!Pattern.matches(PHONE_REG, phone)) {
            return WebResponseVO.errorParam("手机号格式错误");
        }
        MsgSendResultEnum msgSendResultEnum = smsRpc.sendLoginCode(phone);
        if (msgSendResultEnum == MsgSendResultEnum.SEND_SUCCESS) {
            return WebResponseVO.success();
        }
        return WebResponseVO.sysError("短信发送太频繁，请稍后再试");

    }

    @Override
    public WebResponseVO login(String phone, Integer code, HttpServletResponse response) {

        if (StringUtils.isEmpty(phone)) {
            return WebResponseVO.errorParam("手机号不能为空");

        }
        if (!Pattern.matches(PHONE_REG, phone)) {
            return WebResponseVO.errorParam("手机格式异常");
        }
        if (code == null || code < 1000) {
            return WebResponseVO.errorParam("验证码格式异常");
        }
        MsgCheckDTO msgCheckDTO = smsRpc.checkLoginCode(phone, code);

        if (!msgCheckDTO.isCheckStatus()) {
            return WebResponseVO.bizError(msgCheckDTO.getDesc());
        }
        UserLoginDTO login = userPhoneRPC.login(phone);
        if(!login.isLoginSuccess()){
            LOGGER.error("login has error,phone is {}",phone);
            return WebResponseVO.sysError();
        }
        String token = accountTokenRPC.createAndSaveLoginToken(login.getUserId());
        Cookie cookie = new Cookie("qytk", token);
        cookie.setDomain("127.0.0.1");
        cookie.setPath("/");
        //cookie有效期，一般他的默认单位是秒
        cookie.setMaxAge(30 * 24 * 3600);
        //加上它，不然web浏览器不会将cookie自动记录下
        response.addCookie(cookie);
//        response.setHeader("Access-Control-Allow-Credentials", "true");
        // 服务端代码
//        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE,PATCH");

        return WebResponseVO.success(ConvertBeanUtils.convert(login, UserLoginVO.class));


    }
}
