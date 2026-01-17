package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.OrderItem;
import com.spareparts.spareparts_backend.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderItemRepo extends JpaRepository<OrderItem, Integer> {

    // 1. Partner ID එක අනුව විකුණුම් ලැයිස්තුව ලබා ගැනීම (ප්‍රධාන query එක)
    List<OrderItem> findBySpareItemPartnerPartnerIdOrderByOrderCreatedAtDesc(Integer partnerId);

    // 2. Partner කෙනෙකුගේ යම් status එකක පවතින විකුණුම් (eg: SHIPPED items only)
    List<OrderItem> findBySpareItemPartnerPartnerIdAndOrderStatus(Integer partnerId, OrderStatus status);

    // 3. Futuristic: Partner කෙනෙකුගේ නිශ්චිත කාලයක් ඇතුළත ආදායම (Revenue) ගණනය කිරීමට
    @Query("SELECT SUM(oi.totalPrice) FROM OrderItem oi WHERE oi.spareItem.partner.partnerId = :partnerId AND oi.order.status = 'DELIVERED'")
    BigDecimal calculateTotalRevenue(@Param("partnerId") Integer partnerId);

    // 4. යම් Spare Item එකක් දැනටමත් ඕඩර් කර තිබේදැයි බැලීමට (Security/Analytics සඳහා)
    boolean existsBySpareItemSpareItemId(Integer spareItemId);
}
