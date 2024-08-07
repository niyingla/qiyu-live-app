package org.qiyu.live.gift.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopCarItemRespDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 2108658510146078597L;
    private Integer count;
    private SkuInfoDTO skuInfoDTO;
}