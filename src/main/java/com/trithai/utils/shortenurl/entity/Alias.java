package com.trithai.utils.shortenurl.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(
        name = "Alias",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_alias_code", columnNames = "alias")
        },
        indexes = {
            @Index(name = "idx_alias_url", columnList = "alias, url")
        })
public class Alias {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alias_seq_gen")
    @SequenceGenerator(name = "alias_seq_gen", sequenceName = "alias_seq", allocationSize = 10000)
    private Long id;

    @Column(nullable = false, unique = true)
    private String alias;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiredAt;
}
