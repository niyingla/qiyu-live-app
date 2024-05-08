package org.qiyu.live.gift.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ShopCarRespDTO implements Serializable {

    
    private Long userId;
    private Integer roomId;
    private List<ShopCarItemRespDTO> skuCarItemRespDTODTOS;
}