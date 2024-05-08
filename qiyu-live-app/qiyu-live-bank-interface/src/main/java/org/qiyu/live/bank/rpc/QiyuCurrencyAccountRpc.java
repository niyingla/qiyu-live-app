package org.qiyu.live.bank.rpc;

import org.qiyu.live.bank.dto.AccountTradeReqDTO;
import org.qiyu.live.bank.dto.AccountTradeRespDTO;
import org.qiyu.live.bank.dto.QiyuCurrencyAccountDTO;

public interface QiyuCurrencyAccountRpc {
    /**
     * 新增账户
     */
    boolean insertOne(Long userId);

    /**
     * 增加虚拟货币
     */
    void incr(Long userId, int num);

    /**
     * 扣减虚拟币
     */
    void decr(Long userId, int num);

    /**
     * 查询余额
     *
     * @param userId
     * @return
     */
    Integer getBalance(long userId);

    AccountTradeRespDTO consumeForSendGift(AccountTradeReqDTO accountTradeReqDTO);



}
