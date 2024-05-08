package org.qiyu.live.gift.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SkuDetailInfoDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = -1966224160494451603L;
    private Long skuId;
    private Integer skuPrice;
    private String skuCode;
    private String name;
    private String iconUrl;
    private String originalIconUrl;
    private String remark;
    //还有其它复杂数据
}