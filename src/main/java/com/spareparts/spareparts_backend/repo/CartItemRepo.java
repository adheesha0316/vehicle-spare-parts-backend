package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.Cart;
import com.spareparts.spareparts_backend.entity.CartItem;
import com.spareparts.spareparts_backend.entity.SpareItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCartAndSpareItem(Cart cart, SpareItem spareItem);

    List<CartItem> findByCart(Cart cart);

    void deleteByCart(Cart cart);
}
