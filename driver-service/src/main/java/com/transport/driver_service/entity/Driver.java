package com.transport.driver_service.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
public class Driver {

    @Id
    private UUID id;

    @Column (nullable = false, columnDefinition = "text")
    private String name;

    @Column(name = "license_number", nullable = false, columnDefinition = "text")
    private String licenseNumber;

    @Column(nullable = false)
    private boolean active;
}


