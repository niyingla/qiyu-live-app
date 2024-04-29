package org.qiyu.live.gift.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class RedPacketReceiveDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 8090406663558428701L;
    private Integer price;
    private String notifyMsg;
}