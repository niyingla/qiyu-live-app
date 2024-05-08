package org.qiyu.live.bank.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
@Data
public class PayOrderDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 1024766663900835709L;
    private Long id;
    private String orderId;
    private Integer productId;
    private Integer bizCode;
    private Long userId;
    private Integer source;
    private Integer payChannel;
    private Integer status;
    private Date payTime;
}
