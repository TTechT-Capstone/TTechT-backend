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
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.phoneNumber", target = "phoneNumber")
    @Mapping(source = "user.address", target = "address")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.roles", target = "userRole")
    @Mapping(source = "user.createdAt", target = "createdAt")
    @Mapping(source = "user.updatedAt", target = "updatedAt")
    @Mapping(source = "storeName", target = "storeName")
    @Mapping(source = "storeDescription", target = "storeDescription")
    SellerResponse toSellerResponse(Seller seller);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(source = "storeName", target = "storeName")
    @Mapping(source = "storeDescription", target = "storeDescription")
    @Mapping(source = "username", target = "user.username")
    @Mapping(source = "email", target = "user.email")
    @Mapping(source = "firstName", target = "user.firstName")
    @Mapping(source = "lastName", target = "user.lastName") 
    @Mapping(source = "phoneNumber", target = "user.phoneNumber")
    @Mapping(source = "address", target = "user.address")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Seller updateSeller(@MappingTarget Seller seller, SellerUpdateRequest request);
}