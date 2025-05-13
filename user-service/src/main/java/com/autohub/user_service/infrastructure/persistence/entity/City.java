package com.autohub.user_service.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "cities", schema = "autohub", uniqueConstraints = {
    @UniqueConstraint(name = "uk_cities_region_name", columnNames = {"region_id", "name"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class City {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "region_id", nullable = false)
    private Integer regionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", insertable = false, updatable = false)
    private Region region;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "postal_code_pattern", length = 100)
    private String postalCodePattern;
    
    @OneToMany(mappedBy = "city", fetch = FetchType.LAZY)
    private List<Address> addresses;
}
