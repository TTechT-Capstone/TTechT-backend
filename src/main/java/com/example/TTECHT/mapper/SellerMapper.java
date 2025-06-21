package com.example.TTECHT.mapper;

import com.example.TTECHT.dto.repsonse.SellerResponse;
import com.example.TTECHT.dto.request.SellerCreationRequest;
import com.example.TTECHT.dto.request.SellerUpdateRequest;
import com.example.TTECHT.entity.user.Seller;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SellerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Seller toSeller(SellerCreationRequest request);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.createdAt", target = "createdAt")
    @Mapping(source = "user.updatedAt", target = "updatedAt")
    SellerResponse toSellerResponse(Seller seller);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSeller(@MappingTarget Seller seller, SellerUpdateRequest request);
}