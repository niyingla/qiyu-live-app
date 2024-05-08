package org.qiyu.live.id.generate.provider.service.bo;

import java.util.concurrent.atomic.AtomicLong;

public class LocalSeqIdBO {
    //mysql配置的id
    private Integer id;
    //对应分布式id的配置说明
    private String desc;
    //当前在本地内存的id值
    private AtomicLong currentNum;
    //本地内存记录id段的开始位置
    private Long currentStart;
    //本地内存记录id段的结束位置
    private Long nextThreshold;
    //步长
    private Integer step;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Long getCurrentStart() {
        return currentStart;
    }

    public void setCurrentStart(Long currentStart) {
        this.currentStart = currentStart;
    }

    public Long getNextThreshold() {
        return nextThreshold;
    }

    public void setNextThreshold(Long nextThreshold) {
        this.nextThreshold = nextThreshold;
    }

    public AtomicLong getCurrentNum() {
        return currentNum;
    }

    public void setCurrentNum(AtomicLong CurrentNum) {
        this.currentNum = CurrentNum;
    }

    @Override
    public String toString() {
        return "LocalSeqIdBO{" +
                "id=" + id +
                ", desc='" + desc + '\'' +
                ", currentValue=" + currentNum +
                ", currentStart=" + currentStart +
                ", nextThreshold=" + nextThreshold +
                ", step=" + step +
                '}';
    }
}
