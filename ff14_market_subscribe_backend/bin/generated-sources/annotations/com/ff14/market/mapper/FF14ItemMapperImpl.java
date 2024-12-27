package com.ff14.market.mapper;

import com.ff14.market.dto.ItemDTO;
import com.ff14.market.po.FF14ItemPO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-12-27T15:09:58+0800",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.41.0.v20241217-1506, environment: Java 17.0.13 (Eclipse Adoptium)"
)
@Component
public class FF14ItemMapperImpl implements FF14ItemMapper {

    @Override
    public ItemDTO po2dto(FF14ItemPO po) {
        if ( po == null ) {
            return null;
        }

        ItemDTO itemDTO = new ItemDTO();

        itemDTO.setId( po.getId() );
        itemDTO.setName( po.getName() );

        return itemDTO;
    }

    @Override
    public FF14ItemPO dto2po(ItemDTO dto) {
        if ( dto == null ) {
            return null;
        }

        FF14ItemPO fF14ItemPO = new FF14ItemPO();

        fF14ItemPO.setId( dto.getId() );
        fF14ItemPO.setName( dto.getName() );

        return fF14ItemPO;
    }
}
