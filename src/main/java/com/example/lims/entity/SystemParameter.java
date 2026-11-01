package com.example.lims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "system_parameters")
@Getter
@Setter
@NoArgsConstructor
public class SystemParameter {

    @Id
    @Column(length = 100)
    private String key;

    @Column(nullable = false, length = 255)
    private String value;

    @Column(columnDefinition = "text")
    private String description;
}
