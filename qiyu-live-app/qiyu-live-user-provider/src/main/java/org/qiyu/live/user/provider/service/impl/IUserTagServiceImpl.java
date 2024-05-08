package org.qiyu.live.user.provider.service.impl;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.idea.qiyu.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import org.qiyu.live.common.interfaces.ConvertBeanUtils;
import org.qiyu.live.common.interfaces.topic.UserProviderTopicNames;
import org.qiyu.live.user.constants.CacheAsyncDeleteCode;
import org.qiyu.live.user.constants.UserTagFieldNameConstants;
import org.qiyu.live.user.constants.UserTagsEnum;
import org.qiyu.live.user.dto.UserCacheAsyncDeleteDTO;
import org.qiyu.live.user.dto.UserTagDTO;
import org.qiyu.live.user.provider.dao.mapper.IUserTagMapper;
import org.qiyu.live.user.provider.dao.po.UserTagPO;
import org.qiyu.live.user.provider.service.IUserTagService;
import org.qiyu.live.user.utils.TagInfoUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class IUserTagServiceImpl implements IUserTagService {
    @Resource
    IUserTagMapper userTagMapper;

    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    @Resource
    private RedisTemplate<String, UserTagDTO> redisTemplate;

    @Resource
    MQProducer mqProducer;

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        boolean updateStatus=userTagMapper.setTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        if(updateStatus){
            deleteUserTagDTOFromRedis(userId);
            return true;
        }
        String setNxKey = userProviderCacheKeyBuilder.buildTagLockKey(userId);
        String setNxResult= redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer keySerializer = redisTemplate.getKeySerializer();
                RedisSerializer valueSerializer  = redisTemplate.getValueSerializer();
                return  (String) connection.execute("set", keySerializer.serialize(setNxKey),
                        valueSerializer.serialize("-1"),
                        "NX".getBytes(StandardCharsets.UTF_8),
                        "EX".getBytes(StandardCharsets.UTF_8),
                        "3".getBytes(StandardCharsets.UTF_8));
            }
        });
        System.out.println("setNxResult--------->"+setNxResult);
        if(!"OK".equals(setNxResult)){
            return false;
        }
        System.out.println("setNxResult2--------->"+setNxResult);
        UserTagPO userTagPO = userTagMapper.selectById(userId);
        if(userTagPO!=null){
            return false;
        }
        System.out.println("setNxResult3--------->"+setNxResult);
        userTagPO=new UserTagPO();
        userTagPO.setUserId(userId);
        userTagMapper.insert(userTagPO);

        redisTemplate.delete(setNxKey);
        updateStatus=userTagMapper.setTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        System.out.println("测试更新标签成功");
        return updateStatus;
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagsEnum) {
        boolean cancelStatus = userTagMapper.cancelTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        if (!cancelStatus) {
            return false;
        }
        deleteUserTagDTOFromRedis(userId);
        return true;
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        UserTagDTO userTagDTO = this.queryByUserIdFromRedis(userId);
        if(userTagDTO==null){
            return false;
        }
        String fieldName = userTagsEnum.getFieldName();
        if(UserTagFieldNameConstants.TAG_INFO_01.equals(fieldName)){
             return TagInfoUtils.isContain(userTagDTO.getTagInfo01(),userTagsEnum.getTag());
        }else if (UserTagFieldNameConstants.TAG_INFO_02.equals(fieldName)) {
            return TagInfoUtils.isContain(userTagDTO.getTagInfo02(), userTagsEnum.getTag());
        } else if (UserTagFieldNameConstants.TAG_INFO_03.equals(fieldName)) {
            return TagInfoUtils.isContain(userTagDTO.getTagInfo03(), userTagsEnum.getTag());
        }
        return false;
    }
    /**
     * 从redis中删除用户标签对象
     *
     * @param userId
     */
    private void deleteUserTagDTOFromRedis(Long userId) {
        String redisKey = userProviderCacheKeyBuilder.buildTagKey(userId);
        redisTemplate.delete(redisKey);

        UserCacheAsyncDeleteDTO userCacheAsyncDeleteDTO = new UserCacheAsyncDeleteDTO();
        userCacheAsyncDeleteDTO.setCode(CacheAsyncDeleteCode.USER_TAG_DELETE.getCode());
        Map<String,Object> jsonParam = new HashMap<>();
        jsonParam.put("userId",userId);
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

    private UserTagDTO queryByUserIdFromRedis(Long userId){
        String key = userProviderCacheKeyBuilder.buildTagKey(userId);
        UserTagDTO dto = redisTemplate.opsForValue().get(key);
        if(dto!=null){
            return dto;
        }
        UserTagPO userTagPO = userTagMapper.selectById(userId);
        if(userTagPO==null){
            return null;
        }
        dto= ConvertBeanUtils.convert(userTagPO,UserTagDTO.class);
        redisTemplate.opsForValue().set(key,dto);
        return  dto;
    }
}
