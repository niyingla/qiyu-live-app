package org.qiyu.live.gift.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Data
public class RollBackStockDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7069560546309805503L;
    private Long userId;
    private Long orderId;
}
