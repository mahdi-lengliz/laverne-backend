package nst.laverne.lavernebackend.controller;

import nst.laverne.lavernebackend.dto.OrderCreateRequest;
import nst.laverne.lavernebackend.dto.OrderDto;
import nst.laverne.lavernebackend.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto create(@RequestBody OrderCreateRequest request) {
        return orderService.create(request);
    }
}
