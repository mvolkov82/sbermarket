package com.okeandra.demo.controllers;

import java.util.List;
import java.util.Properties;
import java.util.Random;

import com.okeandra.demo.services.processing.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class WebController {
    private SberFromSberFeed deliveryFromSber;
    private InsalesFeed insalesFeed;
    private SberDutyFreeFeed sberDutyFreeFeed;
    private GroupPriceFeed groupPriceFeed;
    private SberFeedAll0DayKapousOllinOneDay sberFeedAll0DayKapousOllinOneDay;
    private OzonFeed ozonFeed;
    private OzonUploadService ozonUploadService;


    public WebController(SberFromSberFeed deliveryFromSber, InsalesFeed insalesFeed, SberDutyFreeFeed sberDutyFreeFeed, GroupPriceFeed groupPriceFeed, SberFeedAll0DayKapousOllinOneDay sberFeedAll0DayKapousOllinOneDay, OzonFeed ozonFeed, OzonUploadService ozonUploadService) {
        this.deliveryFromSber = deliveryFromSber;
        this.insalesFeed = insalesFeed;
        this.sberDutyFreeFeed = sberDutyFreeFeed;
        this.groupPriceFeed = groupPriceFeed;
        this.sberFeedAll0DayKapousOllinOneDay = sberFeedAll0DayKapousOllinOneDay;
        this.ozonFeed = ozonFeed;
        this.ozonUploadService = ozonUploadService;
    }

    @GetMapping("/")
    public String indexPage(Model model) {
        //For no cashing URL
        Random random = new Random();
        model.addAttribute("randomVal", random.nextInt(1000));
        return "index";
    }

    //Установка безопасного остатка
    @GetMapping("/create-feed-okeandra")
    public String createXlsFeedForOkeandra(Model model) {
        List<String> log = insalesFeed.start();
        model.addAttribute("result", log);
        return "result";
    }


    // Из-за херовых сроков пока делаем на все 0 дней кроме Проф.
    @GetMapping("/create-feed-sber")
    public String createFeedForSber(Model model) {
        List<String> log = sberFeedAll0DayKapousOllinOneDay.start();
        model.addAttribute("result", log);
        return "result";
    }


    @GetMapping("/create-sber-from-sber")
    public String createFeedForShipmentFromSberWarehouse(Model model) {
        List<String> log = deliveryFromSber.start();
        model.addAttribute("result", log);
        return "result";
    }


    @GetMapping("/create-sber-duty")
    public String createFeedForSberDutyFree(Model model) {
        List<String> log = sberDutyFreeFeed.start();
        model.addAttribute("result", log);
        return "result";
    }


    @GetMapping("/create-ozon-feed")
    public String createOzonFeed(Model model) {
        List<String> log = ozonFeed.start();
        model.addAttribute("result", log);
        return "result";
    }

    @GetMapping("/create-groupprice")
    public String createGroupPriceFeed(Model model) {
        List<String> log = groupPriceFeed.start();
        model.addAttribute("result", log);
        return "result";
    }

    @PostMapping("/upload-ozon-items")
    public String singleFileUpload(@RequestParam("file") MultipartFile file,
                                   Model model) {

        String log = ozonUploadService.uploadFileAndSendInFtp(file);
        model.addAttribute("result", log);

        return "result";
    }


}
