package com.example.lims.repository;

import com.example.lims.entity.loan.Fine;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FineRepository extends JpaRepository<Fine, UUID> {
    List<Fine> findByLoanId(UUID loanId);

    List<Fine> findAll(Sort sort);

    List<Fine> findByIsPaid(Boolean isPaid, Sort sort);
}
