package com.ecomart.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "admins")
@DiscriminatorValue("ADMIN")
@Getter
@Setter
public class Admin extends User {

    private LocalDate hireDate;
}
