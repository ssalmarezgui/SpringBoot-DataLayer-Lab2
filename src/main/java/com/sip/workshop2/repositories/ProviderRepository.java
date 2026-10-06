package com.sip.workshop2.repositories;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.sip.workshop2.entities.Article;
import com.sip.workshop2.entities.Provider;

@Repository
public interface ProviderRepository extends CrudRepository<Provider, Long> {

    @Query("SELECT a FROM Article a WHERE a.provider.id = ?1")
    List<Article> findArticlesByProvider(long id);
}
