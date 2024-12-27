package com.ff14.market.mapper;

import com.ff14.market.dto.WorldDTO;
import com.ff14.market.po.FF14WorldPO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FF14WorldMapper {
    WorldDTO po2dto(FF14WorldPO po);
}