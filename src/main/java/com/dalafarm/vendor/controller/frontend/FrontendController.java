package com.dalafarm.vendor.controller.frontend;

import com.dalafarm.vendor.model.frontend.OrderBackOfficeModel;
import com.dalafarm.vendor.model.frontend.PagedOrder;
import com.dalafarm.vendor.service.OrderService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by LeeU on 9/3/2017.
 */
@Controller
public class FrontendController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/")
    public String home1() {
        return "home";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/orders")
    public String admin() {
        return "orders";
    }

    @GetMapping("/pagedOrders")
    @ResponseBody
    public PagedOrder pagedOrders(@RequestParam Integer draw, @RequestParam Integer start, @RequestParam Integer length
                                    , @RequestParam(required = false) Map<String, String> paramsMap
                                  ) {
        Iterable<OrderBackOfficeModel> pagedOrders = Collections.EMPTY_LIST;
        if (start != null && length != null) {
            int page = start/length;
            pagedOrders = orderService.getAllOrdersForFrontendWPagingSortingCreatedDateDesc(page, length);
        }
        if(paramsMap != null){
            String searchValue = paramsMap.get("search[value]");
            if (!searchValue.isEmpty()) {
                System.out.println(searchValue);
                pagedOrders = orderService.searchOrdersForFrontendWPagingSortingCreatedDateDesc(searchValue);
                return new PagedOrder(draw, start, length, orderService.countTotalOrders(), pagedOrders.spliterator().getExactSizeIfKnown(), pagedOrders);
            }
        }
        return new PagedOrder(draw, start, length, orderService.countTotalOrders(), orderService.countTotalOrders(), pagedOrders);
    }

    private void addDateFormatter(Model model) {
        SimpleDateFormat displayDateFormatter = new SimpleDateFormat( "yyyy/MM/dd HH:mm zzz" );
        displayDateFormatter.setTimeZone(TimeZone.getTimeZone("GMT+7:00"));
        model.addAttribute( "displayDateFormatter", displayDateFormatter );
    }

    @GetMapping("/edit-order/{orderId}")
    public String editOrder(Model model, @PathVariable String orderId){
        model.addAttribute("order", orderService.getOrderByOrderId(orderId));
        addDateFormatter(model);
        return "edit-order";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/403")
    public String error403() {
        return "403";
    }

    @Data
    private class SearchParams {
        String value;
        boolean regex;
    }
}
