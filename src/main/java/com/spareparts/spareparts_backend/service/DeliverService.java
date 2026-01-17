package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.DeliveryRequestDto;
import com.spareparts.spareparts_backend.dto.DeliveryResponseDto;
import com.spareparts.spareparts_backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeliverService {
    /**
     * Initialize a new delivery for a confirmed order.
     * @param requestDto details including orderId and courierName.
     * @return the created delivery details with tracking number.
     */
    DeliveryResponseDto createDelivery(DeliveryRequestDto requestDto);

    /**
     * Update an existing delivery's status or courier information.
     * @param deliveryId the ID of the delivery to update.
     * @param requestDto the updated data.
     * @return the updated delivery details.
     */
    DeliveryResponseDto updateDelivery(Integer deliveryId, DeliveryRequestDto requestDto);

    /**
     * Retrieve delivery details using the unique tracking number.
     * @param trackingNumber the TRK######### formatted string.
     * @return delivery details.
     */
    DeliveryResponseDto getByTrackingNumber(String trackingNumber);

    /**
     * Retrieve delivery information associated with a specific order.
     * @param orderId ID of the order.
     * @return delivery details.
     */
    DeliveryResponseDto getDeliveryByOrderId(Integer orderId);

    /**
     * Get a paginated list of all deliveries for administrative purposes.
     * @param pageable pagination and sorting information.
     * @return a page of delivery records.
     */
    Page<DeliveryResponseDto> getAllDeliveries(Pageable pageable);

    /**
     * Internal helper to quickly update only the status of a delivery.
     * @param deliveryId ID of the delivery.
     * @param status the new OrderStatus (e.g., SHIPPED, DELIVERED).
     */
    void updateStatus(Integer deliveryId, OrderStatus status);
}
