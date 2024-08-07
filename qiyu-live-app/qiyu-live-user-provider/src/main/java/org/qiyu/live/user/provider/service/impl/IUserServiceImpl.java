package org.qiyu.live.user.provider.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;


import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;

import org.idea.qiyu.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import org.qiyu.live.common.interfaces.ConvertBeanUtils;
import org.qiyu.live.common.interfaces.topic.UserProviderTopicNames;
import org.qiyu.live.user.constants.CacheAsyncDeleteCode;
import org.qiyu.live.user.dto.UserCacheAsyncDeleteDTO;
import org.qiyu.live.user.dto.UserDTO;
import org.qiyu.live.user.provider.dao.mapper.IUserMapper;
import org.qiyu.live.user.provider.dao.po.UserPO;
import org.qiyu.live.user.provider.service.IUserService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class IUserServiceImpl implements IUserService {
    @Resource
    private IUserMapper userMapper;

    @Resource
    private RedisTemplate<String,UserDTO > redisTemplate;

    @Resource
    UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    @Resource
    MQProducer mqProducer;


    @Override
    public UserDTO getByUserId(Long id) {
        System.out.println("getByUserId1");
        System.out.println(id);
        if (id == null) {
            return null;
        }
        System.out.println("getByUserId1");
        String key = userProviderCacheKeyBuilder.buildUserInfoKey(id);
        UserDTO userDTO = redisTemplate.opsForValue().get(key);
        if(userDTO!=null){
            return userDTO;
        }
         userDTO =ConvertBeanUtils.convert(userMapper.selectById(id), UserDTO.class);
        if(userDTO!=null){
            redisTemplate.opsForValue().set(key,userDTO,30, TimeUnit.MINUTES);
        }
        System.out.println("getByUserId!=null");
        return userDTO;
    }

    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUserId() == null) {
            return false;
        }
        int updateStatus = userMapper.updateById(ConvertBeanUtils.convert(userDTO, UserPO.class));
        if (updateStatus > -1) {
            String key = userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId());
            redisTemplate.delete(key);
            UserCacheAsyncDeleteDTO userCacheAsyncDeleteDTO = new UserCacheAsyncDeleteDTO();
            userCacheAsyncDeleteDTO.setCode(CacheAsyncDeleteCode.USER_INFO_DELETE.getCode());
            Map<String,Object> jsonParam = new HashMap<>();
            jsonParam.put("userId",userDTO.getUserId());
            userCacheAsyncDeleteDTO.setJson(JSON.toJSONString(jsonParam));
            Message message = new Message();
            message.setTopic(UserProviderTopicNames.CACHE_ASYNC_DELETE_TOPIC);
            message.setBody(JSON.toJSONString(userCacheAsyncDeleteDTO).getBytes());
            //延迟一秒进行缓存的二次删除
            message.setDelayTimeLevel(1);
            try {
                mqProducer.send(message);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    @Override
    public boolean insertUserInfo(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUserId() == null) {
            return false;
        }
        userMapper.insert(ConvertBeanUtils.convert(userDTO, UserPO.class));
        return true;
    }

    @Override
    public Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Maps.newHashMap();
        }
        userIdList = userIdList.stream().filter(id -> id > 10000).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIdList)) {
            return Maps.newHashMap();
        }
        // redis
        List<String> keyList = new ArrayList<>();
        userIdList.forEach(userId -> {
            keyList.add(userProviderCacheKeyBuilder.buildUserInfoKey(userId));
        });
        List<UserDTO> userDTOList = redisTemplate.opsForValue().multiGet(keyList).stream().filter(x -> x != null).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(userDTOList) && userDTOList.size() == userIdList.size()) {
            return userDTOList.stream().collect(Collectors.toMap(UserDTO::getUserId, x -> x));
        }
        List<Long> userIdInCacheList = userDTOList.stream().map(UserDTO::getUserId).collect(Collectors.toList());
        List<Long> userIdNotInCacheList = userIdList.stream().filter(x -> !userIdInCacheList.contains(x)).collect(Collectors.toList());
        // 多线程查询 替换了union all
        Map<Long, List<Long>> userIdMap = userIdNotInCacheList.stream().collect(Collectors.groupingBy(userId -> userId % 100));
        List<UserDTO> dbQueryResult = new CopyOnWriteArrayList<>();
        userIdMap.values().parallelStream().forEach(queryUserIdList -> {
            dbQueryResult.addAll(ConvertBeanUtils.convertList(userMapper.selectBatchIds(queryUserIdList), UserDTO.class));
        });
        if (!CollectionUtils.isEmpty(dbQueryResult)) {
            Map<String, UserDTO> saveCacheMap = dbQueryResult.stream().collect(Collectors.toMap(userDto -> userProviderCacheKeyBuilder.buildUserInfoKey(userDto.getUserId()), x -> x));
            redisTemplate.opsForValue().multiSet(saveCacheMap);
            //对命令执行批量过期设置操作
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    for (String redisKey : saveCacheMap.keySet()) {
                        operations.expire((K) redisKey, createRandomTime(), TimeUnit.SECONDS);
                    }
                    return null;
                }
            });
            userDTOList.addAll(dbQueryResult);
        }
        return userDTOList.stream().collect(Collectors.toMap(UserDTO::getUserId, x -> x));
    }

    private int createRandomTime() {
        int randomNumSecond = ThreadLocalRandom.current().nextInt(100);
        return randomNumSecond + 30 * 60;
    }
}
