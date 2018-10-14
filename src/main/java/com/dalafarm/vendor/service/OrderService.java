package com.dalafarm.vendor.service;

import com.dalafarm.vendor.model.Order;
import com.dalafarm.vendor.model.OrderProduct;
import com.dalafarm.vendor.model.OrderStatusRequest;
import com.dalafarm.vendor.model.Product;
import com.dalafarm.vendor.model.frontend.OrderBackOfficeModel;
import com.dalafarm.vendor.repository.OrderRepository;
import com.dalafarm.vendor.repository.OrderStatusRequestRepository;
import com.dalafarm.vendor.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by LeeU on 9/3/2017.
 */
@Service
@Slf4j
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderStatusRequestRepository orderStatusRequestRepository;

    @Autowired
    private StatusMapper statusMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    private Iterable<Product> allProducts = null;

    public Iterable<OrderBackOfficeModel> getAllOrdersForFrontendWPaging(int page, int size) {
        Iterable<Order> orders = orderRepository.findAll(new PageRequest(page, size));
        return enrichOrderBackOfficeModels(orders);
    }

    public long countTotalOrders() {
        return orderRepository.count();
    }
    private Iterable<OrderBackOfficeModel> enrichOrderBackOfficeModels(Iterable<Order> orders) {
        if(allProducts == null){
            allProducts = productRepository.findAll();
        }
        return StreamSupport.stream(orders.spliterator(), false).map(o -> {
            List<OrderProduct> orderProductList = o.getOrderProducts();
            o.setProducts(orderProductList.stream().map(op -> {
                Product product = StreamSupport.stream(allProducts.spliterator(), false)
                        .filter(p -> op.getProductId().compareTo(p.getId()) == 0)
                        .findFirst().orElse(null);
                return product;
            })
                    .collect(Collectors.toList()));
            return o;
        })
                .map(orderModelMapper::toOrderBackOfficeModel)
                .collect(Collectors.toList());
    }

    public Iterable<OrderBackOfficeModel> getAllOrdersForFrontend(){
        Iterable<Order> orders = orderRepository.findAll();
        return enrichOrderBackOfficeModels(orders);

    }

    public OrderBackOfficeModel getOrderByOrderId(String orderId) {
        Order order = orderRepository.findByOrderDetailOrderId(orderId);
        Iterable<Product> products = productRepository.findAll();

        List<OrderProduct> orderProductList = order.getOrderProducts();
        order.setProducts(orderProductList.stream().map(op -> {
            Product product = StreamSupport.stream(products.spliterator(), false)
                    .filter(p -> op.getProductId().compareTo(p.getId()) == 0)
                    .findFirst().orElse(null);
            return product;
        }).collect(Collectors.toList()));
        return orderModelMapper.toOrderBackOfficeModel(order);

    }

    public void updateOrderStatus(OrderStatusRequest orderStatusRequest) {
        persistOrderStatusRequest(orderStatusRequest);
        Order order = orderRepository.findByOrderDetailOrderId(orderStatusRequest.getOrderId());
        if (order != null) {
            saveOrderStatus(orderStatusRequest, order);
        } else {
            order = orderRepository.findByOrderDetailVendorOrderId(orderStatusRequest.getVendorOrderId());
            if (order != null) {
                saveOrderStatus(orderStatusRequest, order);
            }else{
                log.warn("Order not found by searching by order id={}", orderStatusRequest.getOrderId());
            }
        }
    }

    private void saveOrderStatus(OrderStatusRequest orderStatusRequest, Order order) {
        order.getOrderDetail().setStatusId(statusMapper.mapVendorStatusIdToSelfStatusId(orderStatusRequest.getStatusId()));
        order.setLastModifiedDate(orderStatusRequest.getActionTime());
        orderRepository.save(order);
    }

    private void persistOrderStatusRequest(OrderStatusRequest orderStatusRequest) {
        orderStatusRequest.setCreatedDate(new Date());
        orderStatusRequestRepository.save(orderStatusRequest);
    }
}
