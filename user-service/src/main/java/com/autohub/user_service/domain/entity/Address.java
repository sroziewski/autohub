package com.autohub.user_service.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@ToString
public class Address {
    private final Long id;
    private final UUID userId;
    private final AddressType addressType;
    private final String companyName;
    private final String street;
    private final Integer cityId;
    private final String cityName;
    private final Integer regionId;
    private final String regionName;
    private final String regionCode;
    private final String postalCode;
    private final Boolean isDefault;
    private final Point coordinates;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    
    public boolean isCompanyAddress() {
        return AddressType.COMPANY.equals(addressType);
    }
    
    public boolean isPersonalAddress() {
        return AddressType.PERSONAL.equals(addressType);
    }
    
    public boolean isDefaultAddress() {
        return Boolean.TRUE.equals(isDefault);
    }
    
    public boolean isValidForAddressType() {
        if (isCompanyAddress() && (companyName == null || companyName.isBlank())) {
            return false;
        }
        
        if (isPersonalAddress() && companyName != null) {
            return false;
        }
        
        return true;
    }
}
