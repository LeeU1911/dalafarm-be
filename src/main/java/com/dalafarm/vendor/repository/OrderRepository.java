package com.dalafarm.vendor.repository;

import com.dalafarm.vendor.model.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by chien on 8/3/17.
 */
public interface OrderRepository  extends PagingAndSortingRepository<Order,Long> {
    Order findByOrderDetailOrderId(String orderId);

    Order findByOrderDetailVendorOrderId(String vendorOrderId);
}
