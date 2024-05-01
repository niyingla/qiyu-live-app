package org.qiyu.live.gift.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PrepareOrderReqDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 5260929476240071986L;
    private Long userId;
    private Integer roomId;
}