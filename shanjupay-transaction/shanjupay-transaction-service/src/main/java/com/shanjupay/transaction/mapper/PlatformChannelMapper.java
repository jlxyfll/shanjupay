package com.shanjupay.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.entity.PlatformChannel;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author author
 * @since 2019-11-15
 */
@Repository
public interface PlatformChannelMapper extends BaseMapper<PlatformChannel> {


    @Select("select pc.*\n" +
            "from platform_pay_channel ppc,\n" +
            "     pay_channel pc,\n" +
            "     platform_channel pla\n" +
            "where ppc.PLATFORM_CHANNEL = pla.CHANNEL_CODE\n" +
            "  and ppc.PAY_CHANNEL = pc.CHANNEL_CODE\n" +
            "  and pla.CHANNEL_CODE = #{platformChannel}")
    public List<PayChannelDTO> selectPayChannelByPlatformChannel(String platformChannel);
}
