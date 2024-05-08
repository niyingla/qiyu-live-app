package org.qiyu.live.bank.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AccountTradeReqDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -3852469488748386015L;
    private long userId;
    private int num;
}
