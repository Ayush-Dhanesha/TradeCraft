package com.tradecraft.trading.domain;

/**
 * Order type: MARKET or LIMIT
 * Maps to Postgres ENUM order_type
 */
public enum OrderType {
    MARKET,
    LIMIT
}

