package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.DeliveryRequestDto;
import com.spareparts.spareparts_backend.dto.DeliveryResponseDto;
import com.spareparts.spareparts_backend.entity.Delivery;
import com.spareparts.spareparts_backend.entity.Order;
import com.spareparts.spareparts_backend.enums.OrderStatus;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.DeliveryRepo;
import com.spareparts.spareparts_backend.repo.OrderRepo;
import com.spareparts.spareparts_backend.service.DeliverService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliverServiceImpl implements DeliverService {
    private final DeliveryRepo deliveryRepo;
    private final OrderRepo orderRepo;

    @Override
    public DeliveryResponseDto createDelivery(DeliveryRequestDto requestDto) {
        // 1. Validate if the order exists
        Order order = orderRepo.findById(requestDto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + requestDto.getOrderId()));

        // 2. Business Logic: Ensure an order doesn't have multiple delivery records
        // You might want to check if a delivery already exists for this order

        Delivery delivery = Delivery.builder()
                .order(order)
                .status(requestDto.getStatus())
                .courierName(requestDto.getCourierName())
                .build();

        // 3. Update the main Order status to match delivery status
        order.setStatus(requestDto.getStatus());
        orderRepo.save(order);

        Delivery savedDelivery = deliveryRepo.save(delivery);
        return mapToResponseDto(savedDelivery);
    }

    @Override
    public DeliveryResponseDto updateDelivery(Integer deliveryId, DeliveryRequestDto requestDto) {
        // 1. Find existing delivery
        Delivery delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with ID: " + deliveryId));

        // 2. Update fields
        delivery.setStatus(requestDto.getStatus());
        if (requestDto.getCourierName() != null) {
            delivery.setCourierName(requestDto.getCourierName());
        }

        // 3. Sync status with the associated Order
        Order order = delivery.getOrder();
        order.setStatus(requestDto.getStatus());
        orderRepo.save(order);

        return mapToResponseDto(deliveryRepo.save(delivery));
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponseDto getByTrackingNumber(String trackingNumber) {
        Delivery delivery = deliveryRepo.findByTrackingNumber(trackingNumber);
        if (delivery == null) {
            throw new ResourceNotFoundException("Delivery not found with tracking number: " + trackingNumber);
        }
        return mapToResponseDto(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponseDto getDeliveryByOrderId(Integer orderId) {
        // Assuming your Repo has a method: findByOrderOrderId(Integer orderId)
        // If not, you can use a custom query or stream through deliveries
        return deliveryRepo.findAll().stream()
                .filter(d -> d.getOrder().getOrderId().equals(orderId))
                .findFirst()
                .map(this::mapToResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("No delivery found for Order ID: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeliveryResponseDto> getAllDeliveries(Pageable pageable) {
        return deliveryRepo.findAll(pageable)
                .map(this::mapToResponseDto);
    }

    @Override
    public void updateStatus(Integer deliveryId, OrderStatus status) {
        Delivery delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        delivery.setStatus(status);
        delivery.getOrder().setStatus(status);

        deliveryRepo.save(delivery);
    }

    // Helper method to convert Entity to DTO
    private DeliveryResponseDto mapToResponseDto(Delivery delivery) {
        return DeliveryResponseDto.builder()
                .deliveryId(delivery.getDeliveryId())
                .orderId(delivery.getOrder().getOrderId())
                .status(delivery.getStatus())
                .trackingNumber(delivery.getTrackingNumber())
                .courierName(delivery.getCourierName())
                .createdAt(delivery.getCreatedAt())
                .updatedAt(delivery.getUpdatedAt())
                .build();
    }
}
