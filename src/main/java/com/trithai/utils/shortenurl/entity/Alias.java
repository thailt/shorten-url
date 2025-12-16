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
@Table(name = "Alias", indexes = {
@Index(name = "idx_alias_url", columnList = "alias, url")
})
public class Alias {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alias_seq_gen")
    @SequenceGenerator(name = "alias_seq_gen", sequenceName = "alias_seq", allocationSize = 1000)
    private Long id;

    private String alias;
    private String url;

    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
}
