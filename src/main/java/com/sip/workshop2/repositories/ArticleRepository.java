package com.sip.workshop2.repositories;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.sip.workshop2.entities.Article;

@Repository
public interface ArticleRepository extends CrudRepository<Article, Long> {
    
    List<Article> findByProviderId(long providerId);
}