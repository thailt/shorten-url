package com.trithai.utils.shortenurl.repository;

import com.trithai.utils.shortenurl.entity.Alias;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AliasRepository extends JpaRepository<Alias, Long> {
    Alias findAliasByAlias(String alias);

    boolean existsAliasByAlias(String key);

    @Query("SELECT a.id, a.alias FROM Alias a WHERE a.id > :lastId ORDER BY a.id ASC")
    List<Object[]> findAliasesWithIdAfterId(long lastId, Pageable pageable);
}
