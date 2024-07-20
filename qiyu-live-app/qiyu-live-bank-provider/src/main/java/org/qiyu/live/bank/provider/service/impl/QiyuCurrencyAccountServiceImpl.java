package org.qiyu.live.bank.provider.service.impl;

import jakarta.annotation.Resource;
import org.idea.qiyu.live.framework.redis.starter.key.BankProviderCacheKeyBuilder;
import org.qiyu.live.bank.constants.TradeTypeEnum;
import org.qiyu.live.bank.dto.AccountTradeReqDTO;
import org.qiyu.live.bank.dto.AccountTradeRespDTO;
import org.qiyu.live.bank.dto.QiyuCurrencyAccountDTO;
import org.qiyu.live.bank.provider.dao.mapper.IQiyuCurrencyAccountMapper;
import org.qiyu.live.bank.provider.dao.po.QiyuCurrencyAccountPO;
import org.qiyu.live.bank.provider.service.IQiyuCurrencyTradeService;
import org.qiyu.live.bank.provider.service.QiyuCurrencyAccountService;
import org.qiyu.live.common.interfaces.enums.CommonStatusEum;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class QiyuCurrencyAccountServiceImpl implements QiyuCurrencyAccountService {

    @Resource
    IQiyuCurrencyAccountMapper qiyuCurrencyAccountMapper;
    @Resource
    RedisTemplate<String,Object> redisTemplate;
    @Resource
    BankProviderCacheKeyBuilder cacheKeyBuilder;
    @Resource
    IQiyuCurrencyTradeService qiyuCurrencyTradeService;
    private ThreadPoolExecutor threadPoolExecutor=new ThreadPoolExecutor(2,5,30,TimeUnit.SECONDS,new ArrayBlockingQueue<>(1000));

    @Override
    public boolean insertOne(Long userId) {
        try {
            QiyuCurrencyAccountPO accountPO = new QiyuCurrencyAccountPO();
            accountPO.setUserId(userId);
            qiyuCurrencyAccountMapper.insert(accountPO);
            return true;
        } catch (Exception e) {
            //有异常但是不抛出，只为了避免重复创建相同userId的账户
        }
        return false;
    }

    @Override
    public void incr(Long userId, int num) {
        String cacheKey = cacheKeyBuilder.buildUserBalance(userId);
        if (redisTemplate.hasKey(cacheKey)) {
            redisTemplate.opsForValue().increment(cacheKey, num);
            redisTemplate.expire(cacheKey, 5, TimeUnit.MINUTES);
        }
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                //分布式架构下，cap理论，可用性和性能，强一致性，柔弱的一致性处理
                //在异步线程池中完成数据库层的扣减和流水记录插入操作，带有事务
                consumeIncrDBHandler(userId, num);
            }
        });


    }

    /**
     * 扣减余额
     * 缓存扣除成功后，异步更新余额和流水
     * @param userId
     * @param num
     */
    @Override
    public void decr(Long userId, int num) {
//        String cacheKey = cacheKeyBuilder.buildUserBalance(userId);
//        redisTemplate.opsForValue().decrement(cacheKey,num);
        String cacheKey = cacheKeyBuilder.buildUserBalance(userId);
        if (redisTemplate.hasKey(cacheKey)) {
            //基于redis的扣减操作
            redisTemplate.opsForValue().decrement(cacheKey, num);
            redisTemplate.expire(cacheKey, 5, TimeUnit.MINUTES);
        }
        //异步线程池中完成数据库层的扣减和流水记录插入操作，带有事务
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                consumeDecrDBHandler(userId,num);
            }
        });
    }

    @Override
    public Integer getBalance(long userId) {
        String cacheKey = cacheKeyBuilder.buildUserBalance(userId);
        Object cacheBalance = redisTemplate.opsForValue().get(cacheKey);
        if(cacheBalance!=null){
            if((Integer) cacheBalance==-1){
                return null;
            }
        }
        Integer balance = qiyuCurrencyAccountMapper.queryBalance(userId);
        if (balance==null) {
            redisTemplate.opsForValue().set(cacheKey,-1,5, TimeUnit.MINUTES);
            return null;
        }
        redisTemplate.opsForValue().set(cacheKey,balance,30,TimeUnit.MINUTES);
        return balance;
    }


    @Override
    public AccountTradeRespDTO consumeForSendGift(AccountTradeReqDTO accountTradeReqDTO) {
        //余额判断
        long userId = accountTradeReqDTO.getUserId();
        int num= accountTradeReqDTO.getNum();
        Integer balance = this.getBalance(userId);
        System.out.println("userId=" + userId);
        System.out.println(balance);
        //判断缓存余额
        if (balance == null || balance < num) {
            return AccountTradeRespDTO.buildFail(userId,"账户余额不足",1);
        }

        this.decr(userId,num);
        //扣减余额
        return AccountTradeRespDTO.buildSuccess(userId,"消费成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public void consumeIncrDBHandler(long userId, int num) {
        //更新db，插入db
        qiyuCurrencyAccountMapper.incr(userId, num);
        //流水记录
        qiyuCurrencyTradeService.insertOne(userId, num, TradeTypeEnum.SEND_GIFT_TRADE.getCode());
    }

    @Transactional(rollbackFor = Exception.class)
    public void consumeDecrDBHandler(long userId, int num) {
        //更新db，插入db
        qiyuCurrencyAccountMapper.decr(userId, num);
        //流水记录
        qiyuCurrencyTradeService.insertOne(userId, num * -1, TradeTypeEnum.SEND_GIFT_TRADE.getCode());
    }


    @Override
    public AccountTradeRespDTO consume(AccountTradeReqDTO accountTradeReqDTO) {
//        long userId = accountTradeReqDTO.getUserId();
//        int num = accountTradeReqDTO.getNum();
//        //首先判断账户余额是否充足，考虑无记录的情况
//        QiyuCurrencyAccountDTO accountDTO = this.getByUserId(userId);
//        if (accountDTO == null) {
//            return AccountTradeRespDTO.buildFail(userId, "账户未有初始化", 1);
//        }
//        if (!accountDTO.getStatus().equals(CommonStatusEum.VALID_STATUS.getCode())) {
//            return AccountTradeRespDTO.buildFail(userId, "账号异常", 2);
//        }
//        if (accountDTO.getCurrentBalance() - num < 0) {
//            return AccountTradeRespDTO.buildFail(userId, "余额不足", 3);
//        }
//        this.decr(userId, num);
        return AccountTradeRespDTO.buildSuccess(-1L, "扣费成功");
    }
}
