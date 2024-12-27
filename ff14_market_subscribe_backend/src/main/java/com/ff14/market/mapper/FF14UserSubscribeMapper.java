package com.ff14.market.mapper;

import com.ff14.market.dto.UserSubscribeReqDTO;
import com.ff14.market.dto.UserSubscribeResDTO;
import com.ff14.market.po.FF14UserSubPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/26 15:58
 */
@Mapper(componentModel = "spring", uses = {FF14UserMapper.class, FF14ItemMapper.class})
public interface FF14UserSubscribeMapper {

	UserSubscribeResDTO po2resDto(FF14UserSubPO po);

	FF14UserSubPO reqDto2po(UserSubscribeReqDTO dto);

	@Mapping(target = "po.id", ignore = true)
	void reqDto2po(UserSubscribeReqDTO dto, @MappingTarget FF14UserSubPO po);

}
