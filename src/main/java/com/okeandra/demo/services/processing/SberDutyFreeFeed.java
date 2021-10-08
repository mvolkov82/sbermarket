package com.okeandra.demo.services.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.okeandra.demo.exceptions.FtpTransportException;
import com.okeandra.demo.models.Item;
import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.WarehouseItemCount;
import com.okeandra.demo.models.YmlObject;
import com.okeandra.demo.services.creators.XmlFinalCreator;
import com.okeandra.demo.services.parsers.ExcelParser;
import com.okeandra.demo.services.transport.impl.FtpTransporterImpl;
import com.okeandra.demo.services.transport.impl.XmlTransporterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/*  1. Скачать фид для Duty Free https://okeandra.ru/marketplace/84930.xml
 *  2. Получить YMLObject распарсив YML на 3 части - хедер, body, футер.
 *  3. Скачать XLS-фид с FTP
 *  4. Распарсить XLS - получить HashMap с остатками
 *  5. Из Body получить List<Offer>
 *  6. Исключить из List<Offer> варианты, которых нет на остатках склада PL
 *  7. Из полученного YML создать новый файл
 *  8. Отправить новый YML в FTP
 * */

@Component
public class SberDutyFreeFeed implements Processing {
    private FtpTransporterImpl ftp;
    private ExcelParser excelParser;
    private XmlTransporterImpl xmlTransporter;

    private XmlFinalCreator xmlFinalCreator;

    @Value("${ftp.xls.source.file}")
    private String xlsSourceFile;

    @Value("${ftp.xls.source.directory}")
    private String xlsSourceFtpFolder;

    @Value("${xml.dutyfree.url}")
    private String dutyFreeXmlSourceUrl;

    @Value("${xml.dutyfree.result.file}")
    private String finishedFeedForDutyFree;

    @Value("${ftp.xml.destination.directory}")
    private String ftpSberDirectory;

    @Value("${dutyfree.shipment.days}")
    private int dutyFreeShipmentDays;

    @Autowired
    public SberDutyFreeFeed(FtpTransporterImpl ftp, ExcelParser excelParser, XmlTransporterImpl xmlTransporter, XmlFinalCreator xmlFinalCreator) {
        this.ftp = ftp;
        this.excelParser = excelParser;
        this.xmlTransporter = xmlTransporter;
        this.xmlFinalCreator = xmlFinalCreator;
    }

    @Override
    public List<String> start() {
        List<String> resultText = new ArrayList<>();

        //1 Скачать фид для Duty Free и положить в Root
        boolean isYmlReceipted = xmlTransporter.getXmlFromUrlAndSave(dutyFreeXmlSourceUrl, getFilenameFromPath(dutyFreeXmlSourceUrl));
        String message;
        if (isYmlReceipted) {
            message = String.format("YML - duty free Insales получен %s", dutyFreeXmlSourceUrl);
        } else {
            message = String.format("Ошибка при получении YML с Insales %s", dutyFreeXmlSourceUrl);
        }
        resultText.add(message);

        YmlObject ymlObject = null;
        boolean isYmlParsed = false;
        if (isYmlReceipted) {
            // 2. Получить YMLObject распарсив YML на 3 части - хедер, body, футер.
            try {
                ymlObject = YmlObject.getYmlObject(getFilenameFromPath(dutyFreeXmlSourceUrl));
                String headerText = ymlObject.getHeaderContent();
                String fixedCategoryName = headerText.replace("<category id=\"8406956\">Duty Free</category>", "<category id=\"8406956\">Парфюмерия</category>");
                ymlObject.setHeaderContent(fixedCategoryName);
                message = "Файл yml корректно распознан";
                isYmlParsed = true;

            } catch (Exception e) {
                message = "Ошибка при парсинге YML-файла";
            }
            resultText.add(message);
        }

        // 3. Скачать XLS-фид с FTP
        boolean isXlsSourceCopied = false;
        if (isYmlParsed) {
            try {
                isXlsSourceCopied = ftp.downloadFileFromFtp(xlsSourceFtpFolder, xlsSourceFile);
                message = "Файл " + xlsSourceFile + " скопирован с FTP";

            } catch (FtpTransportException e) {
                message = "Ошибка при копировании файла " + xlsSourceFile + " с FTP. Ошибка: " + e.getMessage();
            }
            resultText.add(message);
        }

        //4. Распарсить XLS - получить HashMap с остатками
        boolean isXlsParsed = false;
        Map<Item, List<WarehouseItemCount>> stock = null;
        if (isXlsSourceCopied) {
            try {
                stock = excelParser.getWarehouseStock(xlsSourceFile);
                message = "Остатки из XLS получены";
                isXlsParsed = true;

            } catch (Exception e) {
                message = "Ошибка при получении остатков из XLS. " + e.getMessage();
            }
            resultText.add(message);
        }

        //5. Из Body получить List<Offer>
        List<Offer> offers = null;
        boolean isYmlBodyReceived = false;
        try {
            offers = ymlObject.getBody();
            if (offers != null & offers.size() != 0) {
                message = "Список офферов получен из YML. Количество парфюма: " + offers.size();
                isYmlBodyReceived = true;
            } else {
                message = "Офферы из YML не получены";
            }
        } catch (Exception e) {
            message = "Ошибка при получении офферов из YML: " + e.getMessage();
        }
        resultText.add(message);

//        List<Offer> offersOnlyInStockInPL = new ArrayList<>(100);
//        boolean isDeletedItemsWithoutStock = false;
        if (isYmlBodyReceived) {

            /*
            //6. Исключить из List<Offer> варианты, которых нет на остатках склада PL
            // по факту - добавить в новый лист только те, у которых есть остаток на PL

            for (Offer offer : offers) {
                Item item = new Item(offer.getVendorCode());
                int itemStock = stock.get(item).get(0).getCount();
                if (itemStock > 0) {
                    //Установка значения = остатку на PL
                    offer.setInStock(itemStock);

                    offersOnlyInStockInPL.add(offer);
                }
            }
            ymlObject.setBody(offersOnlyInStockInPL);
            isDeletedItemsWithoutStock = true;
            resultText.add("Количество наименований парфюма на складе PL: " + offersOnlyInStockInPL.size());
        } */

            //6. Новая версия. Дата отгрузки Duty Free = 4 дня

            for (Offer offer : offers) {
                offer.setDays(dutyFreeShipmentDays);
            }
            ymlObject.setBody(offers);
//            ymlObject.setBody(offersOnlyInStockInPL);
//            isDeletedItemsWithoutStock = true;
//            resultText.add("Количество наименований парфюма на складе PL: " + offersOnlyInStockInPL.size());
        }

//        if (isDeletedItemsWithoutStock) {

            //7. Из полученного YML создать новый файл
            //Превращаем YMLObject снова в XML и сохраняем его.
            xmlFinalCreator.saveXmlFile(finishedFeedForDutyFree, ymlObject);
            resultText.add("Сохранен новый YML-фид: " + finishedFeedForDutyFree);

            try {
                ftp.uploadFileToFtp(ftpSberDirectory, finishedFeedForDutyFree, finishedFeedForDutyFree);
                resultText.add("YML-фид для DutyFree отправлен на FTP");
            } catch (FtpTransportException e) {
                System.out.println(e.getMessage());
                resultText.add("Ошибка при отправке фида на FTP: " + e.getMessage());
            }
//        }

//        resultText.add("Duty Free парфюм в наличии: ");
//        int i = 0;
//        for (Offer offerPL : offersOnlyInStockInPL) {
//            i++;
//            resultText.add(String.format("%s) %s %s : %s", i, offerPL.getVendorCode(), offerPL.getName(), offerPL.getInStock()));
//        }

        return resultText;
    }

    private String getFilenameFromPath(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        return fileName;
    }
}
