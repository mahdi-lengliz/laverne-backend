package nst.laverne.lavernebackend.service;

import java.util.List;
import nst.laverne.lavernebackend.dto.OrderCreateRequest;
import nst.laverne.lavernebackend.dto.OrderDto;
import nst.laverne.lavernebackend.dto.StatsDto;
import nst.laverne.lavernebackend.model.OrderStatus;

public interface OrderService {
    OrderDto create(OrderCreateRequest request);

    List<OrderDto> findAll();

    OrderDto updateStatus(Long id, OrderStatus status);

    StatsDto stats();
}
