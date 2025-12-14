package com.trithai.utils.shortenurl.repository;

import com.trithai.utils.shortenurl.entity.Alias;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface AliasRepository extends CrudRepository<Alias, Long> {
    Alias findAliasByAlias(String alias);

    boolean existsAliasByAlias(String key);

    @Query(
            value = "select a.alias from alias a"
            , nativeQuery = true)
    Set<String> findAllAlias();
}
