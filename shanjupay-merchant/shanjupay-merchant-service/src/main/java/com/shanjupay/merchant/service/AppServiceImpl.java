package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.convert.AppConvert;
import com.shanjupay.merchant.entity.App;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.AppMapper;
import com.shanjupay.merchant.mapper.MerchantMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

/**
 * @title: AppServiceImpl
 * @Author Tang Xiaojiang
 * @Date: 2020/11/22 12:11
 * @Version 1.0
 */
@Service
public class AppServiceImpl implements AppService {

    @Autowired
    private AppMapper appMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public AppDTO createApp(Long merchantId, AppDTO appDTO) throws BusinessException {
        /**
         * 1）校验商户是否通过资质审核
         * 如果商户资质审核没有通过不允许创建应用。
         * 2）生成应用ID
         * 应用Id使用UUID方式生成。
         * 3）保存商户应用信息
         * 应用名称需要校验唯一性。
         */
        // 校验参数合法性
        if (merchantId == null || appDTO == null || StringUtils.isBlank(appDTO.getAppName())) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        // 1）校验商户是否通过资质审核
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        if (!"2".equals(merchant.getAuditStatus())) {
            throw new BusinessException(CommonErrorCode.E_200003);
        }
        String appName = appDTO.getAppName();
        if (isExistAppName(appName)) {
            throw new BusinessException(CommonErrorCode.E_200004);
        }
        // 2）生成应用UUID
        String appId = UUID.randomUUID().toString();
        // 3）将appId 传入app
        App app = AppConvert.INSTANCE.appDTO2App(appDTO);
        app.setAppId(appId);
        app.setMerchantId(merchantId);
        // 4）保存商户应用信息
        appMapper.insert(app);
        return AppConvert.INSTANCE.app2AppDTO(app);
    }


    /**
     * 校验应用名称是否被使用
     *
     * @param appName
     * @return
     */
    private Boolean isExistAppName(String appName) {
        Integer count = appMapper.selectCount(new LambdaQueryWrapper<App>().eq(App::getAppName, appName));
        return count > 0;
    }

    @Override
    public List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException {
        List<App> appList = appMapper.selectList(new LambdaQueryWrapper<App>().eq(App::getMerchantId, merchantId));
        List<AppDTO> appDTOList = AppConvert.INSTANCE.AppList2DTO(appList);
        return appDTOList;
    }

    @Override
    public AppDTO getAppById(String appId) throws BusinessException {
        App app = appMapper.selectOne(new LambdaQueryWrapper<App>().eq(App::getAppId, appId));
        AppDTO appDTO = AppConvert.INSTANCE.app2AppDTO(app);
        return appDTO;
    }

    /**
     * 查询应用是否属于某个商户
     *
     * @param appId
     * @param merchantId
     * @return
     * @throws BusinessException
     */
    @Override
    public Boolean queryAppInMerchant(String appId, Long merchantId) throws BusinessException {
        if (appId == null || merchantId == null) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        Integer count = appMapper.selectCount(new LambdaQueryWrapper<App>().eq(App::getAppId, appId).eq(App::getMerchantId, merchantId));
        return count > 0;
    }
}