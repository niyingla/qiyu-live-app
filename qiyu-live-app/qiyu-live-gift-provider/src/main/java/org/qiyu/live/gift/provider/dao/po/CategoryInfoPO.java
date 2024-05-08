package org.qiyu.live.gift.provider.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 类目表
 */
@Data
@TableName("t_category_info")
public class CategoryInfoPO {
    
    @TableId(type = IdType.AUTO)
    private Integer level;
    private String categoryName;
    private Integer parentId;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}