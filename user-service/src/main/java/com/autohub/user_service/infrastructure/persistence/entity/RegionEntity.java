package com.autohub.user_service.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "regions", schema = "autohub", uniqueConstraints = {
    @UniqueConstraint(name = "regions_name_key", columnNames = "name"),
    @UniqueConstraint(name = "regions_code_key", columnNames = "code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, length = 100, unique = true)
    private String name;
    
    @Column(length = 20, unique = true)
    private String code;

    @OneToMany(mappedBy = "region", fetch = FetchType.LAZY)
    private List<CityEntity> cities;

    @OneToMany(mappedBy = "region", fetch = FetchType.LAZY)
    private List<Address> addresses;

}
