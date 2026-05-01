package com.nisum.userapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;


@Table(name = "phones")
@Data
@NoArgsConstructor
public class Phone {

    @Id
    private UUID id;
    private String number;
    private String citycode;
    private String contrycode;

    @Column("user_id")
    private UUID userId;

}
