package com.okeandra.demo.controllers;

import java.io.IOException;
import java.util.List;

import com.okeandra.demo.services.processing.DeliveryFromSberWarehouse;
import com.okeandra.demo.services.processing.InsalesFeed;
import com.okeandra.demo.services.processing.SberFeed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class WebController {
    private DeliveryFromSberWarehouse deliveryFromSber;
    private InsalesFeed insalesFeed;
    private SberFeed sberFeed;

    @Autowired
    public WebController(DeliveryFromSberWarehouse deliveryFromSber, InsalesFeed insalesFeed, SberFeed sberFeed) {
        this.deliveryFromSber = deliveryFromSber;
        this.insalesFeed = insalesFeed;
        this.sberFeed = sberFeed;
    }

    @GetMapping("/")
    public String indexPage() {
        return "index";
    }

    @GetMapping("/create-feed-okeandra")
    public String createXlsFeedForOkeandra(Model model) {
        List<String> log = insalesFeed.start();
        model.addAttribute("result", log);
        return "result";
    }

    @GetMapping("/create-feed-sber")
    public String createFeedForSber(Model model) {
        List<String> log = sberFeed.start();
        model.addAttribute("result", log);
        return "result";
    }

    @GetMapping("/create-sber-from-sber")
    public String createFeedForShipmentFromSberWarehouse(Model model) {
        List<String> log = deliveryFromSber.start();
        model.addAttribute("result", log);
        return "result";
    }

//    @GetMapping("/set-limits")
//    public String setLimits(Model model) {
//        List<String> log = xlsLimiter.start();
//        model.addAttribute("result", log);
//        return "result";
//    }


}
