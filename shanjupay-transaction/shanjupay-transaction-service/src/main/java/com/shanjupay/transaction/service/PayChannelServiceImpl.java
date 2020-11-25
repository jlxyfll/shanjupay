package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.LambdaQueryChainWrapper;
import com.shanjupay.common.cache.Cache;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.RedisUtil;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import com.shanjupay.transaction.convert.PayChannelParamConvert;
import com.shanjupay.transaction.convert.PayOrderConvert;
import com.shanjupay.transaction.convert.PlatformChannelConvert;
import com.shanjupay.transaction.entity.AppPlatformChannel;
import com.shanjupay.transaction.entity.PayChannel;
import com.shanjupay.transaction.entity.PayChannelParam;
import com.shanjupay.transaction.entity.PlatformChannel;
import com.shanjupay.transaction.mapper.AppPlatformChannelMapper;
import com.shanjupay.transaction.mapper.PayChannelParamMapper;
import com.shanjupay.transaction.mapper.PlatformChannelMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @title: PayChannelServiceImpl
 * @Author Tang Xiaojiang
 * @Date: 2020/11/22 15:33
 * @Version 1.0
 */
@Service
public class PayChannelServiceImpl implements PayChannelService {
    @Autowired
    private PlatformChannelMapper platformChannelMapper;

    @Autowired
    private AppPlatformChannelMapper appPlatformChannelMapper;

    @Autowired
    private PayChannelParamMapper payChannelParamMapper;

    @Resource
    private Cache cache;

    @Override
    public List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException {
        // 查询platform_channel表的所有数据
        List<PlatformChannel> platformChannelList = platformChannelMapper.selectList(null);
        List<PlatformChannelDTO> platformChannelDTOList = PlatformChannelConvert.INSTANCE.listentity2listdto(platformChannelList);
        return platformChannelDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException {
        // 根据应用的id和服务类型的code来查询，如果已经绑定，则不再插入，否则插入记录
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>()
                .eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCodes));
        if (appPlatformChannel == null) {
            AppPlatformChannel entity = new AppPlatformChannel();
            entity.setAppId(appId);
            entity.setPlatformChannel(platformChannelCodes);
            appPlatformChannelMapper.insert(entity);
        }
    }

    @Override
    public int queryAppBindPlatformChannel(String appId, String platformChannel) throws BusinessException {
        Integer count = appPlatformChannelMapper.selectCount(new LambdaQueryWrapper<AppPlatformChannel>().eq(AppPlatformChannel::getAppId, appId).eq(AppPlatformChannel::getPlatformChannel, platformChannel));
        if (count > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannel) throws BusinessException {
        List<PayChannelDTO> payChannelDTOList = platformChannelMapper.selectPayChannelByPlatformChannel(platformChannel);
        return payChannelDTOList;
    }

    @Override
    public void savePayChannelParam(PayChannelParamDTO payChannelParam) throws BusinessException {
        // 判断参数合法性
        if (payChannelParam == null
                || StringUtils.isBlank(payChannelParam.getAppId())
                || StringUtils.isBlank(payChannelParam.getPayChannel())
                || StringUtils.isBlank(payChannelParam.getPlatformChannelCode())) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }

        // 根据应用、服务类型、支付渠道查询一条记录
        // 根据应用、服务类型查询应用与服务类型的绑定id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(payChannelParam.getAppId(), payChannelParam.getPlatformChannelCode());
        if (appPlatformChannelId == null) {
            //应用未绑定该服务类型不可进行支付渠道参数配置
            throw new BusinessException(CommonErrorCode.E_300010);
        }
        // 根据应用与服务类型的绑定id和支付渠道查询PayChannelParam的一条记录

        PayChannelParam entity = payChannelParamMapper.selectOne(new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId).eq(PayChannelParam::getPayChannel, payChannelParam.getPayChannel()));
        if (entity != null) {
            // 如果存在配置则更新
            entity.setChannelName(payChannelParam.getChannelName());
            entity.setParam(payChannelParam.getParam());
            payChannelParamMapper.updateById(entity);
        } else {
            // 否则添加配置
            PayChannelParam entityNew = PayChannelParamConvert.INSTANCE.dto2entity(payChannelParam);
            entityNew.setId(null);
            entityNew.setAppPlatformChannelId(appPlatformChannelId);
            payChannelParamMapper.insert(entityNew);
        }
        //保存到redis
        updateCache(payChannelParam.getAppId(), payChannelParam.getPlatformChannelCode());
    }

    /**
     * 根据应用和服务类型将查询到支付渠道参数配置列表写入redis
     *
     * @param appId
     * @param platformChannelCode
     */
/*//    private void updateCache(String appId, String platformChannelCode) {
//        // 得到redis中key（付渠道参数配置列表的key）
//        // 格式：SJ_PAY_PARAN：应用id：服务类型code，
//        String redisKey = RedisUtil.keyBuilder(appId, platformChannelCode);
//        // 根据key查询redis
//        Boolean exists = cache.exists(redisKey);
//        if (exists) {
//            cache.del(redisKey);
//        }
//        // 根据应用id和服务类型code查询支付渠道参数列表，将写入支付渠道参数列表redis
////        List<PayChannelParamDTO> payChannelParamDTOList = queryPayChannelParamByAppAndPlatform(appId, platformChannelCode);
//        // 将支付渠道参数列表存储到redis
//        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannelCode);
//
//        if (appPlatformChannelId != null) {
//            List<PayChannelParam> payChannelParamList = payChannelParamMapper.selectList(new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
//            List<PayChannelParamDTO> payChannelParamDTOS = PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParamList);
//            cache.set(redisKey, JSON.toJSON(payChannelParamDTOS).toString());
//        }
//    }*/
    private void updateCache(String appId, String platformChannelCode) {

        //得到redis中key(付渠道参数配置列表的key)
        //格式：SJ_PAY_PARAM:应用id:服务类型code，例如：SJ_PAY_PARAM：ebcecedd-3032-49a6-9691-4770e66577af：shanju_c2b
        String redisKey = RedisUtil.keyBuilder(appId, platformChannelCode);
        //根据key查询redis
        Boolean exists = cache.exists(redisKey);
        if (exists) {
            cache.del(redisKey);
        }
        //根据应用id和服务类型code查询支付渠道参数
        //根据应用和服务类型找到它们绑定id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannelCode);
        if (appPlatformChannelId != null) {
            //应用和服务类型绑定id查询支付渠道参数记录
            List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
            List<PayChannelParamDTO> payChannelParamDTOS = PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParams);
            //将payChannelParamDTOS转成json串存入redis
            cache.set(redisKey, JSON.toJSON(payChannelParamDTOS).toString());
        }

    }


    private Long selectIdByAppPlatformChannel(String appId, String platformChannelCode) {
        // 根据应用、服务类型查询应用与服务类型的绑定id
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>().eq(AppPlatformChannel::getAppId, appId).eq(AppPlatformChannel::getPlatformChannel, platformChannelCode));
        if (appPlatformChannel != null) {
            Long appPlatformChannelId = appPlatformChannel.getId();
            return appPlatformChannelId;
        }
        return null;
    }

    @Override
    public List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel) throws BusinessException {
        // 先从redis中查询，如果有则返回
        String redisKey = RedisUtil.keyBuilder(appId, platformChannel);
        Boolean exists = cache.exists(redisKey);
        if (exists) {
            String PayChannelParamDTO_String = cache.get(redisKey);
            List<PayChannelParamDTO> payChannelParamDTOList = JSON.parseArray(PayChannelParamDTO_String, PayChannelParamDTO.class);
            return payChannelParamDTOList;
        }

        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannel);
        if (appPlatformChannelId == null) {
            return null;
        }
        List<PayChannelParam> payChannelParamList = payChannelParamMapper.selectList(new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
        List<PayChannelParamDTO> payChannelParamDTOList = PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParamList);
        //保存到redis
        updateCache(appId, platformChannel);
        return payChannelParamDTOList;

    }

    @Override
    public PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel, String payChannel) throws BusinessException {
        List<PayChannelParamDTO> payChannelParamDTOList = queryPayChannelParamByAppAndPlatform(appId, platformChannel);
        if (payChannelParamDTOList != null) {
            for (PayChannelParamDTO payChannelParamDTO : payChannelParamDTOList) {
                if (payChannelParamDTO.getPayChannel().equals(payChannel)) {
                    return payChannelParamDTO;
                }
            }
        }
        return null;
    }
}
