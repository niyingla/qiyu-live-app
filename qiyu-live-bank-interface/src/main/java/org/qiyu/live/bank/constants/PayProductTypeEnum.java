package org.qiyu.live.bank.constants;

public enum PayProductTypeEnum {
    
    QIYU_COIN(0, "直播间充值-旗鱼虚拟币产品");
    
    Integer code;
    String desc;

    PayProductTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}