package nst.laverne.lavernebackend.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import nst.laverne.lavernebackend.dto.OrderCreateRequest;
import nst.laverne.lavernebackend.dto.OrderDto;
import nst.laverne.lavernebackend.dto.OrderItemRequest;
import nst.laverne.lavernebackend.dto.StatsDto;
import nst.laverne.lavernebackend.exception.BadRequestException;
import nst.laverne.lavernebackend.exception.ResourceNotFoundException;
import nst.laverne.lavernebackend.mapper.OrderMapper;
import nst.laverne.lavernebackend.model.CustomerOrder;
import nst.laverne.lavernebackend.model.OrderItem;
import nst.laverne.lavernebackend.model.OrderStatus;
import nst.laverne.lavernebackend.model.Product;
import nst.laverne.lavernebackend.repository.OrderRepository;
import nst.laverne.lavernebackend.repository.ProductRepository;
import nst.laverne.lavernebackend.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    private static final BigDecimal FREE_SHIPPING_LIMIT = BigDecimal.valueOf(300);
    private static final BigDecimal SHIPPING_PRICE = BigDecimal.valueOf(15);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    public OrderDto create(OrderCreateRequest request) {
        validate(request);
        CustomerOrder order = new CustomerOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerName(request.customerName().trim());
        order.setCustomerEmail(request.customerEmail());
        order.setCustomerPhone(request.customerPhone().trim());
        order.setAddress(request.address().trim());
        order.setCity(request.city().trim());
        order.setNotes(request.notes());
        order.setCreatedAt(Instant.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));
            int quantity = itemRequest.quantity() == null ? 1 : itemRequest.quantity();
            if (quantity < 1) {
                throw new BadRequestException("Quantite invalide");
            }
            if (product.getStock() < quantity) {
                throw new BadRequestException("Stock insuffisant pour " + product.getName());
            }

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            item.setPerfumeSize(product.getPerfumeSize());
            item.setImageUrl(product.getImageUrl());
            item.setEmoji(product.getEmoji());
            order.addItem(item);

            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        BigDecimal shipping = subtotal.compareTo(FREE_SHIPPING_LIMIT) >= 0 ? BigDecimal.ZERO : SHIPPING_PRICE;
        order.setShipping(shipping);
        order.setTotal(subtotal.add(shipping));
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(orderMapper::toDto).toList();
    }

    @Override
    public OrderDto updateStatus(Long id, OrderStatus status) {
        CustomerOrder order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Commande introuvable"));
        OrderStatus previous = order.getStatus();

        if ((status == OrderStatus.CONFIRMED || status == OrderStatus.DELIVERED) && (previous == OrderStatus.PENDING || previous == OrderStatus.CANCELLED)) {
            for (OrderItem item : order.getItems()) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));
                if (product.getStock() < item.getQuantity()) {
                    throw new BadRequestException("Stock insuffisant pour " + product.getName());
                }
                product.setStock(product.getStock() - item.getQuantity());
            }
        }

        if (status == OrderStatus.CANCELLED && (previous == OrderStatus.CONFIRMED || previous == OrderStatus.DELIVERED)) {
            for (OrderItem item : order.getItems()) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));
                product.setStock(product.getStock() + item.getQuantity());
            }
        }

        order.setStatus(status);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public StatsDto stats() {
        List<CustomerOrder> orders = orderRepository.findAll();
        BigDecimal revenue = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .map(CustomerOrder::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new StatsDto(orders.size(), orderRepository.countByStatus(OrderStatus.PENDING), revenue, productRepository.count());
    }

    private void validate(OrderCreateRequest request) {
        if (request.customerName() == null || request.customerName().isBlank()) {
            throw new BadRequestException("Nom requis");
        }
        if (request.customerPhone() == null || !request.customerPhone().matches(".*[0-9]{8}.*")) {
            throw new BadRequestException("Telephone invalide");
        }
        if (request.address() == null || request.address().isBlank()) {
            throw new BadRequestException("Adresse requise");
        }
        if (request.city() == null || request.city().isBlank()) {
            throw new BadRequestException("Ville requise");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new BadRequestException("Panier vide");
        }
    }

    private String generateOrderNumber() {
        return "LVN-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
