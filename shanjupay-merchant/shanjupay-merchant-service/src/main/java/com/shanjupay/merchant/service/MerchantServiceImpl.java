package com.shanjupay.merchant.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.PageVO;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.convert.MerchantConvert;
import com.shanjupay.merchant.convert.StaffConvert;
import com.shanjupay.merchant.convert.StoreConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.entity.Staff;
import com.shanjupay.merchant.entity.Store;
import com.shanjupay.merchant.entity.StoreStaff;
import com.shanjupay.merchant.mapper.MerchantMapper;
import com.shanjupay.merchant.mapper.StaffMapper;
import com.shanjupay.merchant.mapper.StoreMapper;
import com.shanjupay.merchant.mapper.StoreStaffMapper;
import com.shanjupay.user.api.TenantService;
import com.shanjupay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.shanjupay.user.api.dto.tenant.TenantDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @title: MerchantServiceImpl
 * @Author Tang Xiaojiang
 * @Date: 2020/11/20 15:05
 * @Version 1.0
 */
@Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private StoreStaffMapper storeStaffMapper;

    @Reference
    private TenantService tenantService;

    /**
     * 根据ID查询详细信息
     *
     * @param merchantId
     * @return
     */
    @Override
    public MerchantDTO queryMerchantById(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        MerchantDTO merchantDTO = MerchantConvert.INSTANCE.merchant2MerchantDTO(merchant);
        return merchantDTO;
    }

    /**
     * 注册商户接口实现，用来接收服务注册时的手机号，账号，密码
     *
     * @param merchantDTO
     * @return
     */
    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException {
        // 1.校验
        if (merchantDTO == null) {
            // 传入对象为空
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        // 2.校验手机号
        if (StringUtils.isBlank(merchantDTO.getMobile())) {
            // 手机号为空
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        // 3.校验手机号合法性
        if (!PhoneUtil.isMatches(merchantDTO.getMobile())) {
            // 手机号不合法
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        // 4.联系人非空校验
        if (StringUtils.isBlank(merchantDTO.getUsername())) {
            // 联系人为空
            throw new BusinessException(CommonErrorCode.E_100110);
        }
        // 校验密码非空
        if (StringUtils.isBlank(merchantDTO.getPassword())) {
            throw new BusinessException(CommonErrorCode.E_100111);
        }
        // 5.校验手机号唯一性
        LambdaQueryWrapper<Merchant> lambdaQueryWrapper = new LambdaQueryWrapper<Merchant>().eq(Merchant::getMobile, merchantDTO.getMobile());
        Integer count = merchantMapper.selectCount(lambdaQueryWrapper);
        if (count > 0) {
            // 手机号已存在
            throw new BusinessException(CommonErrorCode.E_100113);
        }

        CreateTenantRequestDTO createTenantRequestDTO = new CreateTenantRequestDTO();
        // 租户的账号信息
        createTenantRequestDTO.setMobile(merchantDTO.getMobile());
        createTenantRequestDTO.setUsername(merchantDTO.getUsername());
        createTenantRequestDTO.setPassword(merchantDTO.getPassword());
        // 表示该租户类型是商户
        createTenantRequestDTO.setTenantTypeCode("shanju-merchant");
        // 设置租户套餐为初始化套餐餐
        createTenantRequestDTO.setBundleCode("shanju-merchant");
        // 新增租户并设置为管理员
        createTenantRequestDTO.setName(merchantDTO.getUsername());
        log.info("商户中心调用统一账号服务，新增租户和账号");
        TenantDTO tenantDTO = tenantService.createTenantAndAccount(createTenantRequestDTO);
        if (tenantDTO == null || tenantDTO.getId() == null) {
            throw new BusinessException(CommonErrorCode.E_200012);
        }
        // 获取住户的id
        Long tenantDTOId = tenantDTO.getId();
        Integer count1 = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantDTOId));
        if (count1 > 0) {
            throw new BusinessException(CommonErrorCode.E_200017);
        }
        // 设置商户所属租户
        merchantDTO.setTenantId(tenantDTO.getId());
        // 设置审核状态，注册时默认为"0"
        merchantDTO.setAuditStatus("0");
        Merchant merchantNew = MerchantConvert.INSTANCE.merchantDTO2Merchant(merchantDTO);
        log.info("保存商户注册信息:{}", merchantNew);
        merchantMapper.insert(merchantNew);

        // 新增门店，创建根门店
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setMerchantId(merchantNew.getId());
        storeDTO.setStoreName("根门店");
//        storeDTO.setStoreStatus(true);
        StoreDTO storeDTONew = creatStore(storeDTO);
        log.info("门店信息：{}", JSON.toJSONString(storeDTO));

        // 新增员工，并设置归属门店
        StaffDTO staffDTO = new StaffDTO();
        staffDTO.setMerchantId(merchantNew.getId());
        staffDTO.setMobile(merchantNew.getMobile());
        staffDTO.setStoreId(storeDTONew.getId());
        staffDTO.setUsername(merchantDTO.getUsername());
//        staffDTO.setStaffStatus(true);
        StaffDTO staffDTONew = createStaff(staffDTO);

        // 为门店设置管理员
        bindStaffToStore(storeDTONew.getId(), staffDTONew.getId());

//        // 将dto转化为entity
//        Merchant merchant = MerchantConvert.INSTANCE.merchantDTO2Merchant(merchantDTO);
//        // 设置审核信息
//        merchant.setAuditStatus("0");
//        // 保存商户
//        merchantMapper.insert(merchant);

        // 将entity转成DTO
        MerchantDTO merchantDTONew = MerchantConvert.INSTANCE.merchant2MerchantDTO(merchantNew);
        return merchantDTONew;
    }

    /**
     * 商户下新增门店
     *
     * @param storeDTO
     * @return
     * @throws BusinessException
     */
    @Override
    public StoreDTO creatStore(StoreDTO storeDTO) throws BusinessException {
        if (storeDTO == null) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        Store store = StoreConvert.INSTANCE.storeDTO2Store(storeDTO);
        log.info("商户下新增门店:{}", JSON.toJSONString(store));
        storeMapper.insert(store);
        StoreDTO storeDTONew = StoreConvert.INSTANCE.store2StoreDTO(store);
        return storeDTONew;
    }

    /**
     * 商户新增员工
     *
     * @param staffDTO
     * @return
     * @throws BusinessException
     */
    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException {
        if (staffDTO == null
                || StringUtils.isBlank(staffDTO.getMobile())
                || StringUtils.isBlank(staffDTO.getUsername())
                || staffDTO.getStoreId() == null) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        // 1、检验手机号格式及是否存在
        String mobile = staffDTO.getMobile();
        if (StringUtils.isBlank(mobile)) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        // 在同一个商户下员工的账号唯一
        Boolean existStaffByMobile = isExistStaffByMobile(mobile, staffDTO.getMerchantId());
        if (existStaffByMobile) {
            throw new BusinessException(CommonErrorCode.E_100113);
        }
        // 在同一个商户下员工的手机号唯一
        Boolean existStaffByUserName = isExistStaffByUserName(staffDTO.getUsername(), staffDTO.getMerchantId());
        if (existStaffByUserName) {
            throw new BusinessException(CommonErrorCode.E_100114);
        }

        Staff staff = StaffConvert.INSTANCE.storeDTO2Store(staffDTO);
        log.info("商户下新增员工");
        staffMapper.insert(staff);
        StaffDTO staffDTONew = StaffConvert.INSTANCE.staff2StaffDTO(staff);
        return staffDTONew;
    }


    /**
     * 根据手机号判断员工是否已在指定商户存在
     *
     * @param mobile
     * @param merchantId
     * @return
     */
    private Boolean isExistStaffByMobile(String mobile, Long merchantId) {
        Integer count = staffMapper.selectCount(new LambdaQueryWrapper<Staff>().eq(Staff::getMobile, mobile)
                .eq(Staff::getMerchantId, merchantId));
        return count > 0;
    }

    /**
     * 根据账号判断员工是否已在指定商户存在
     *
     * @param userName
     * @param merchantId
     * @return
     */
    private Boolean isExistStaffByUserName(String userName, Long merchantId) {
        Integer count = staffMapper.selectCount(new LambdaQueryWrapper<Staff>().eq(Staff::getUsername, userName)
                .eq(Staff::getMerchantId, merchantId));
        return count > 0;
    }

    /**
     * 将员工设置为门店的管理员
     *
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    @Override
    public void bindStaffToStore(Long storeId, Long staffId) throws BusinessException {
        StoreStaff storeStaff = new StoreStaff();
        storeStaff.setStoreId(storeId);
        storeStaff.setStaffId(staffId);

        storeStaffMapper.insert(storeStaff);
    }

    /**
     * 查询租户下的商户
     *
     * @param tenantId
     * @return
     * @throws BusinessException
     */
    @Override
    public MerchantDTO queryMerchantByTenantId(Long tenantId) throws BusinessException {
        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantId));
        MerchantDTO merchantDTO = MerchantConvert.INSTANCE.merchant2MerchantDTO(merchant);
        return merchantDTO;
    }

    /**
     * 分页条件查询商户下门店
     *
     * @param storeDTO
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public PageVO<StoreDTO> queryStoreByPage(StoreDTO storeDTO, Integer pageNo, Integer pageSize) throws BusinessException {
        // 创建页
        Page<Store> page = new Page<>(pageNo, pageSize);
        // 构造查询条件
        if (storeDTO == null || storeDTO.getMerchantId() == null) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        LambdaQueryWrapper<Store> lambdaQueryWrapper = new LambdaQueryWrapper<Store>().eq(Store::getMerchantId, storeDTO.getMerchantId());
        // 执行查询
        IPage<Store> storeIPage = storeMapper.selectPage(page, lambdaQueryWrapper);
        // entity转dto
        List<StoreDTO> storeDTOList = StoreConvert.INSTANCE.storeList2DTO(storeIPage.getRecords());
        // 封装结果集
        PageVO<StoreDTO> pageVO = new PageVO<>(storeDTOList, storeIPage.getTotal(), pageNo, pageSize);
        return pageVO;
    }

    /**
     * 查询门店是否属于某商户
     *
     * @param storeId
     * @param merchantId
     * @return
     * @throws BusinessException
     */
    @Override
    public Boolean queryStoreInMerchant(Long storeId, Long merchantId) throws BusinessException {
        if (storeId == null || merchantId == null) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        Integer count = storeMapper.selectCount(new LambdaQueryWrapper<Store>().eq(Store::getId, storeId).eq(Store::getMerchantId, merchantId));
        return count > 0;
    }
}
