package com.pedidofacil.repositories;

import com.pedidofacil.models.Customer;
import com.pedidofacil.models.Order;
import com.pedidofacil.models.PaymentMethod;
import com.pedidofacil.repositories.projections.DailySalesView;
import com.pedidofacil.repositories.projections.PaymentDistributionView;
import com.pedidofacil.repositories.projections.ProductSalesView;
import com.pedidofacil.repositories.projections.TicketAverageView;
import org.springframework.data.domain.Pageable;
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

    @Query("select o.paymentMethod as paymentMethod, sum(o.total) as total from Order o where (:start is null or o.createdAt >= :start) and (:end is null or o.createdAt <= :end) group by o.paymentMethod")
    List<PaymentDistributionView> sumByPaymentMethod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("select o.paymentMethod as paymentMethod, count(o) as orders, avg(o.total) as average from Order o where (:start is null or o.createdAt >= :start) and (:end is null or o.createdAt <= :end) group by o.paymentMethod")
    List<TicketAverageView> ticketAverageByPayment(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("select o.customer.id as customerId, o.customer.name as customerName, sum(o.total) as total from Order o where o.customer is not null and (:start is null or o.createdAt >= :start) and (:end is null or o.createdAt <= :end) and (:method is null or o.paymentMethod = :method) group by o.customer.id, o.customer.name order by sum(o.total) desc")
    List<com.pedidofacil.repositories.projections.TopCustomerView> topCustomers(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("method") PaymentMethod method,
            Pageable pageable
    );

    @Query("select function('strftime','%Y-%m-%d', o.createdAt) as day, sum(o.total) as total from Order o where (:start is null or o.createdAt >= :start) and (:end is null or o.createdAt <= :end) group by function('strftime','%Y-%m-%d', o.createdAt) order by day")
    List<DailySalesView> dailySales(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("select p.id as productId, concat(p.name, coalesce(concat(' (', p.brand, ')'), '')) as productName, sum(oi.quantity) as quantity, sum(oi.subtotal) as total " +
           "from OrderItem oi join oi.order o join oi.product p " +
           "where (:start is null or o.createdAt >= :start) and (:end is null or o.createdAt <= :end) " +
           "group by p.id, p.name, p.brand order by sum(oi.subtotal) desc")
    List<ProductSalesView> productSales(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}
