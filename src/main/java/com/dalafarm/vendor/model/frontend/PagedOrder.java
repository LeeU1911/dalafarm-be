package com.dalafarm.vendor.model.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedOrder {
    private int draw;

    private int start;

    private int length;

    private long recordsTotal;

    private long recordsFiltered;

    @JsonProperty("data")
    Iterable<OrderBackOfficeModel> pagedOrders;
}
