package org.qiyu.live.im.dto;

import java.io.Serial;
import java.io.Serializable;


public class ImMsgBody implements Serializable {


    @Serial
    private static final long serialVersionUID = -5693518461104801119L;
    /**
     * 接入im服务的各个业务线id
     */
    private int appId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 从业务服务中获取，用于在im服务建立连接时使用，从中获取userId与userId进行比较
     */
    private String token;
    /**
     * 业务类型标识
     */
    private int bizCode;
    /**
     * 和业务服务进行消息传递
     */
    private String data;

    @Override
    public String toString() {
        return "ImMsgBody{" +
                "appId=" + appId +
                ", userId=" + userId +
                ", token='" + token + '\'' +
                ", bizCode=" + bizCode +
                ", data='" + data + '\'' +
                '}';
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getBizCode() {
        return bizCode;
    }

    public void setBizCode(int bizCode) {
        this.bizCode = bizCode;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
