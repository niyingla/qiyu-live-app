package org.qiyu.live.living.interfaces.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class LivingPkRespDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = -9051471143224979729L;
    private boolean onlineStatus;
    private String msg;
}