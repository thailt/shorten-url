package com.trithai.utils.shortenurl.repository;

import com.trithai.utils.shortenurl.entity.Alias;
import org.springframework.data.repository.CrudRepository;

public interface AliasRepository extends CrudRepository<Alias, Long> {
    Alias findAliasByAlias(String alias);
}
