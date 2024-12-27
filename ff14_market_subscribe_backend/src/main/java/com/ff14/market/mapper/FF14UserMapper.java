package com.ff14.market.mapper;

import com.ff14.market.dto.UserRegisterReqDTO;
import com.ff14.market.dto.UserResDTO;
import com.ff14.market.po.FF14UserPO;
import org.mapstruct.Mapper;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/20 9:38
 */
@Mapper(componentModel = "spring")
public interface FF14UserMapper {

	FF14UserPO dto2po(UserRegisterReqDTO dto);

	UserResDTO po2dto(FF14UserPO po);

}
