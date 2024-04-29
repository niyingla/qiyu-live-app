package org.qiyu.live.gift.provider.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_red_packet_config")
public class RedPacketConfigPO {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Long anchorId;
    /**
     * 红包雨开启时间
     */
    private Date startTime;
    /**
     * 总共领取的红包
     */
    private Integer totalGet;
    /**
     * 金额
     */
    private Integer totalGetPrice;
    /**
     * 最大领取金额
     */
    private Integer maxGetPrice;
    private Integer status;
    private Integer totalPrice;
    private Integer totalCount;
    private String configCode;
    private String remark;
    private Date createTime;
    private Date updateTime;
}