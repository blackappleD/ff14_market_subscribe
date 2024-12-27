package com.ff14.market.mapper;

import com.ff14.market.dto.ItemDTO;
import com.ff14.market.po.FF14ItemPO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FF14ItemMapper {
	ItemDTO po2dto(FF14ItemPO po);

	FF14ItemPO dto2po(ItemDTO dto);
}