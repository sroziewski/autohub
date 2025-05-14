package com.autohub.user_service.infrastructure.persistence.entity;

import com.autohub.user_service.infrastructure.persistence.converter.PointConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "addresses", schema = "autohub", indexes = {
    @Index(name = "idx_addresses_user_id", columnList = "user_id"),
    @Index(name = "idx_addresses_city_id", columnList = "city_id"),
    @Index(name = "idx_addresses_region_id", columnList = "region_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    private AddressType addressType;
    
    @Column(name = "company_name")
    private String companyName;
    
    @Column(nullable = false)
    private String street;
    
    @Column(name = "city_id", nullable = false)
    private Integer cityId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", insertable = false, updatable = false)
    private CityEntity city;
    
    @Column(name = "region_id", nullable = false)
    private Integer regionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", insertable = false, updatable = false)
    private RegionEntity region;
    
    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;
    
    @Column(name = "is_default")
    private Boolean isDefault;
    
    @Column(name = "coordinates", columnDefinition = "point")
    @Convert(converter = PointConverter.class)
    private Point coordinates;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum AddressType {
        PERSONAL,
        COMPANY
    }
}
