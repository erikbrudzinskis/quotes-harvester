package com.erikbrudzinskis.quotesharvester.repository;

import com.erikbrudzinskis.quotesharvester.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Integer> {
}
