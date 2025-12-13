package com.trithai.utils.shortenurl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Entity
@Data
@Table(name = "Alias")
public class Alias {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alias_seq_gen")
    @SequenceGenerator(name = "alias_seq_gen", sequenceName = "alias_seq", allocationSize = 10)
    private Long id;

    private String alias;
    private String url;
}
