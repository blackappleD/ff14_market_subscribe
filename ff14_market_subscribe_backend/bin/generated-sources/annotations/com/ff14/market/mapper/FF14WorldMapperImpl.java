package com.ff14.market.mapper;

import com.ff14.market.dto.WorldDTO;
import com.ff14.market.po.FF14WorldPO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-12-27T15:09:58+0800",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.41.0.v20241217-1506, environment: Java 17.0.13 (Eclipse Adoptium)"
)
@Component
public class FF14WorldMapperImpl implements FF14WorldMapper {

    @Override
    public WorldDTO po2dto(FF14WorldPO po) {
        if ( po == null ) {
            return null;
        }

        WorldDTO worldDTO = new WorldDTO();

        worldDTO.setId( po.getId() );
        worldDTO.setLevel( po.getLevel() );
        worldDTO.setName( po.getName() );
        worldDTO.setWorldId( po.getWorldId() );

        return worldDTO;
    }
}
