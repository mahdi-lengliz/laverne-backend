package nst.laverne.lavernebackend.mapper;

import nst.laverne.lavernebackend.dto.OrderDto;
import nst.laverne.lavernebackend.dto.OrderItemDto;
import nst.laverne.lavernebackend.model.CustomerOrder;
import nst.laverne.lavernebackend.model.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    public OrderDto toDto(CustomerOrder order) {
        return new OrderDto(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getCustomerPhone(),
                order.getAddress(),
                order.getCity(),
                order.getNotes(),
                order.getTotal(),
                order.getShipping(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getItems().stream().map(this::toItemDto).toList()
        );
    }

    private OrderItemDto toItemDto(OrderItem item) {
        return new OrderItemDto(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getPerfumeSize(),
                item.getImageUrl(),
                item.getEmoji()
        );
    }
}
