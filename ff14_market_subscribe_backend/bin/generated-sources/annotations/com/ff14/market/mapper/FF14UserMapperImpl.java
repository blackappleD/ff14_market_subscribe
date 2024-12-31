package com.ff14.market.mapper;

import com.ff14.market.dto.UserRegisterReqDTO;
import com.ff14.market.dto.UserResDTO;
import com.ff14.market.po.FF14UserPO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-12-30T17:50:50+0800",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.41.0.v20241217-1506, environment: Java 17.0.13 (Eclipse Adoptium)"
)
@Component
public class FF14UserMapperImpl implements FF14UserMapper {

    @Override
    public FF14UserPO dto2po(UserRegisterReqDTO dto) {
        if ( dto == null ) {
            return null;
        }

        FF14UserPO fF14UserPO = new FF14UserPO();

        fF14UserPO.setEmail( dto.getEmail() );
        fF14UserPO.setPassword( dto.getPassword() );
        fF14UserPO.setUserAccount( dto.getUserAccount() );
        fF14UserPO.setUserName( dto.getUserName() );

        return fF14UserPO;
    }

    @Override
    public UserResDTO po2dto(FF14UserPO po) {
        if ( po == null ) {
            return null;
        }

        UserResDTO userResDTO = new UserResDTO();

        userResDTO.setEmail( po.getEmail() );
        userResDTO.setId( po.getId() );
        userResDTO.setUserName( po.getUserName() );

        return userResDTO;
    }
}
