package com.ff14.market.mapper;

import com.ff14.market.dto.SubscribeCfgReqDTO;
import com.ff14.market.dto.SubscribeCfgResDTO;
import com.ff14.market.po.FF14SubscribeCfgPO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FF14SubscribeCfgMapper {

	FF14SubscribeCfgPO dto2po(SubscribeCfgReqDTO dto);

	void dto2po(SubscribeCfgReqDTO dto, @MappingTarget FF14SubscribeCfgPO po);

	SubscribeCfgResDTO po2dto(FF14SubscribeCfgPO po);

}