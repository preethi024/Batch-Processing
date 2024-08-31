package com.springbatch.BatchProcessing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springbatch.BatchProcessing.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Integer> {

}
