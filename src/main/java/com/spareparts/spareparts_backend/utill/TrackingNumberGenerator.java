package com.spareparts.spareparts_backend.utill;

import com.spareparts.spareparts_backend.repo.DeliveryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class TrackingNumberGenerator {
    private final DeliveryRepo deliveryRepo;
    private final Random random = new Random();
    private static final int MAX_ATTEMPTS = 20;

    /**
     * Generate a super-safe tracking number: TRK#########
     * Combines deliveryId, timestamp, and random number to fit 9 digits.
     */
    public String generate(Integer deliveryId) {
        String trackingNumber;
        int attempts = 0;

        do {
            trackingNumber = generateHybrid(deliveryId);

            attempts++;
            if (attempts > MAX_ATTEMPTS) {
                throw new IllegalStateException(
                        "Unable to generate unique tracking number after " + MAX_ATTEMPTS + " attempts"
                );
            }
        } while (deliveryRepo.existsByTrackingNumber(trackingNumber));

        return trackingNumber;
    }

    /**
     * Hybrid generator:
     * 1. Uses deliveryId as seed
     * 2. Mixes timestamp and small random number
     * 3. Reduces to 9 digits
     */
    private String generateHybrid(Integer deliveryId) {
        long timestampPart = System.currentTimeMillis() % 1_000_000; // last 6 digits of timestamp
        int randomPart = random.nextInt(1000); // 0-999
        int deliveryPart = deliveryId != null ? deliveryId % 1000 : random.nextInt(1000); // deliveryId mod 1000

        int combined = (int)((timestampPart + randomPart + deliveryPart) % 1_000_000_000);
        return "TRK" + String.format("%09d", combined);
    }
}
