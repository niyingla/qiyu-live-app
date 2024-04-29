package org.qiyu.live.gift.bo;

import lombok.Data;
import org.qiyu.live.gift.dto.RedPacketConfigReqDTO;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SendRedPacketBO implements Serializable {
    @Serial
    private static final long serialVersionUID = 5385732721750964626L;
    private Integer price;
    private RedPacketConfigReqDTO reqDTO;
}