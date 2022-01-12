package com.okeandra.demo.controllers;

import java.util.List;
import java.util.Random;

import com.okeandra.demo.services.processing.FileInfoService;
import com.okeandra.demo.services.processing.SberDutyFreeFeed;
import com.okeandra.demo.services.processing.SberFeedAll0DayKapousOllinOneDay;
import com.okeandra.demo.services.processing.SberFromSberFeed;
import com.okeandra.demo.services.processing.InsalesFeed;
import com.okeandra.demo.services.processing.SberFeed;
//import com.okeandra.demo.services.transport.email.SendEmail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class WebController {
    private SberFromSberFeed deliveryFromSber;
    private InsalesFeed insalesFeed;
    private SberFeed sberFeed;
    private SberDutyFreeFeed sberDutyFreeFeed;
    private SberFeedAll0DayKapousOllinOneDay sberFeedAll0DayKapousOllinOneDay;
    private FileInfoService fileInfoService;
//    private SendEmail sendEmail;


    public WebController(SberFromSberFeed deliveryFromSber, InsalesFeed insalesFeed, SberFeed sberFeed, SberDutyFreeFeed sberDutyFreeFeed, SberFeedAll0DayKapousOllinOneDay sberFeedAll0DayKapousOllinOneDay, FileInfoService fileInfoService) {
        this.deliveryFromSber = deliveryFromSber;
        this.insalesFeed = insalesFeed;
        this.sberFeed = sberFeed;
        this.sberDutyFreeFeed = sberDutyFreeFeed;
        this.sberFeedAll0DayKapousOllinOneDay = sberFeedAll0DayKapousOllinOneDay;
        this.fileInfoService = fileInfoService;
    }

    @GetMapping("/")
    public String indexPage(Model model) {

//        String date1CItemsXls = fileInfoService.getExcelSourceFeedDate();
//        model.addAttribute("source_1CItems_xls_date", date1CItemsXls);
//
//        String date1CItemsFeedXLS = fileInfoService.getExcelWithLimitsDate();
//        model.addAttribute("limitedFeedDate", date1CItemsFeedXLS);
//
//        String basicFeedForSberDate = fileInfoService.getXmlBasicFeedDate();
//        model.addAttribute("basicFeedForSberDate", basicFeedForSberDate);
//
//        String dutyFreeFeedForSberDate = fileInfoService.getXmlDutyFreeFeedDate();
//        model.addAttribute("dutyFreeFeedForSberDate", dutyFreeFeedForSberDate);
//
//        String dayPerDayFileDate = fileInfoService.getDayPerDayFileDate();
//        model.addAttribute("dayPerDayFileDate", dayPerDayFileDate);
//
//        String shipmentFromSberWarehouseFeedDate = fileInfoService.getShipmentFormSberWarehouseFeedDate();
//        model.addAttribute("shipmentFromSberWarehouseFeedDate", shipmentFromSberWarehouseFeedDate);
//
//        String itemsFromSberWarehouseFileDate = fileInfoService.getItemsShipmentFormSberWarehouseFileDate();
//        model.addAttribute("itemsFromSberWarehouseFileDate", itemsFromSberWarehouseFileDate);
//
//
        //For no cashing URL
        Random random = new Random();
        model.addAttribute("randomVal", random.nextInt(1000));
        return "index";
    }

//    @GetMapping("/ajax")
//    public String ajaxPage() {
//        return "ajax";
//    }

//    @GetMapping("/query")
//    public String ajaxQuery() {
//        System.out.println("YAHOOOOOOOOOOOOOO");
//        return "query";
//    }

    //Установка безопасного остатка
    @GetMapping("/create-feed-okeandra")
    public String createXlsFeedForOkeandra(Model model) {
        List<String> log = insalesFeed.start();
        model.addAttribute("result", log);
        return "result";
    }

    // Из-за херовых сроков пока делаем на все 0 дней кроме Проф.
//    @GetMapping("/create-feed-sber")
//    public String createFeedForSber(Model model) {
//        List<String> log = sberFeed.start();
//        model.addAttribute("result", log);
//        return "result";
//    }

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

//    Отправка почты не работает в хероку
//    @GetMapping("/send-email")
//    public String sendEmail(Model model) {
//        try {
//            sendEmail.send();
//        } catch (IOException e) {
//            System.out.println("IOEXCEPTION!!!");
//        }
//        return "result";
//    }



//    @GetMapping("/set-limits")
//    public String setLimits(Model model) {
//        List<String> log = xlsLimiter.start();
//        model.addAttribute("result", log);
//        return "result";
//    }


}
