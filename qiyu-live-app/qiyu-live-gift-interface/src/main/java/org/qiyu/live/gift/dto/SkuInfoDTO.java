package org.qiyu.live.gift.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SkuInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -1738535046222555442L;
    private Long id;
    private Long skuId;
    private Integer skuPrice;
    private String skuCode;
    private String name;
    private String iconUrl;
    private String originalIconUrl;
    private Integer status;
    private String remark;
}