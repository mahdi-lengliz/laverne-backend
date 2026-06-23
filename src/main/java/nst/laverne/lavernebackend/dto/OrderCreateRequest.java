package nst.laverne.lavernebackend.dto;

import java.util.List;

public record OrderCreateRequest(
        String customerName,
        String customerEmail,
        String customerPhone,
        String address,
        String city,
        String notes,
        List<OrderItemRequest> items
) {
}
