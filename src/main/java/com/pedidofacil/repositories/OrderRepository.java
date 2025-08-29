package com.pedidofacil.repositories;

import com.pedidofacil.models.Customer;
import com.pedidofacil.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select coalesce(max(o.number), 0) from Order o")
    Long findMaxNumber();

    @Query("select o from Order o left join fetch o.customer where (:start is null or o.createdAt >= :start) and (:end is null or o.createdAt <= :end) and (:customer is null or o.customer = :customer) order by o.createdAt desc")
    List<Order> findHistory(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("customer") Customer customer
    );
}
