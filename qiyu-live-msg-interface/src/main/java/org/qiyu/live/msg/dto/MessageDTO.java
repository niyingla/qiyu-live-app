package org.qiyu.live.msg.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


public class MessageDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = -759018873561012748L;
    private Long userId;
    private Long  objectId;
//    //发送人名称
//    private String senderName;
//    //发送人头像
//    private String senderAvtar;
    /**
     * 消息类型
     */
    private Integer type;
    /**
     * 消息内容
     */
    private String content;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    @Override
    public String toString() {
        return "MessageDTO{" +
                "userId=" + userId +
                ", objectId=" + objectId +
                ", type=" + type +
                ", content='" + content + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    private Date createTime;
    private Date updateTime;


}
