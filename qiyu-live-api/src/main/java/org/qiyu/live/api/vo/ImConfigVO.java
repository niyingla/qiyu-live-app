package org.qiyu.live.api.vo;

import lombok.Data;

@Data
public class ImConfigVO {
    
    private String token;
    private String wsImServerAddress;
    private String tcpImServerAddress;
}