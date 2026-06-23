package nst.laverne.lavernebackend.dto;

import java.math.BigDecimal;

public record StatsDto(long orders, long pendingOrders, BigDecimal revenue, long products) {
}
