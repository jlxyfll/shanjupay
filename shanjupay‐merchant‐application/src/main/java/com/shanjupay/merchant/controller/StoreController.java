package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.PageVO;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.common.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @title: StoreController
 * @Author Tang Xiaojiang
 * @Date: 2020/11/27 15:42
 * @Version 1.0
 */
@Slf4j
@RestController
@Api(value = "商户平台-门店管理", tags = "商户平台-门店管理", description = "商户平台-门店的增删改查")
public class StoreController {
    @Reference
    private MerchantService merchantService;

    @ApiOperation("分页条件查询商户下门店")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNo", value = "页码", required = true, dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", required = true, dataType = "Integer", paramType = "query")})
    @PostMapping(value = "/my/stores/merchants/page")
    public PageVO<StoreDTO> queryStoreByPage(@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
        if (pageNo == null || pageSize == null) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setMerchantId(SecurityUtil.getMerchantId());
        PageVO<StoreDTO> storeByPage = merchantService.queryStoreByPage(storeDTO, pageNo, pageSize);
        return storeByPage;
    }
}
