package com.ecomart.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "staffs")
@DiscriminatorValue("STAFF")
@Getter
@Setter
public class Staff extends User {

    private LocalDate hireDate;
}