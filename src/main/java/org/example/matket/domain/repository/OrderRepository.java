package org.example.matket.domain.repository;

import org.example.matket.domain.Order;
import org.example.matket.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {


    @Query("SELECT o FROM Order o WHERE o.comment.post.id = :postId AND o.comment.authorKey = :authorKey AND o.status = :status")
    List<Order> findActiveOrdersByPostAndAuthor(@Param("postId") Long postId, @Param("authorKey") String authorKey, @Param("status") OrderStatus status);

}
