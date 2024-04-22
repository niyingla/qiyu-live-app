package org.qiyu.live.living.interfaces.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class LivingRoomReqDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8487748005203681856L;
    private Integer id;
    private Long anchorId;
    private Long pkObjId;
    private String roomName;
    private Integer roomId;
    private String covertImg;
    private Integer type;
    private Integer appId;
    private int page;
    private int pageSize;

}
