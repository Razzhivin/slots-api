package com.slotsapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /**
     * Поддомен аккаунта amoCRM (уникален)
     * Например: "myclinic" из "myclinic.amocrm.ru"
     */
    @Column(name = "amocrm_subdomain", unique = true)
    private String amocrmSubdomain;

    /**
     * Числовой идентификатор аккаунта в amoCRM (получаем через API)
     */
    @Column(name = "amocrm_account_id")
    private Long amocrmAccountId;

    /**
     * Флаг активности компании (для биллинга и контроля доступа)
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // ========== Связи ==========

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AmoToken> amoTokens = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApiKey> apiKeys = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resource> resources = new ArrayList<>();
}