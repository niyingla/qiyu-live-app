package org.qiyu.live.api.vo.resp;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class LivingPkRespDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 5777278109557913600L;
    private boolean onlineStatus;
    private String msg;
}