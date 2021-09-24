package com.okeandra.demo.services.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.okeandra.demo.exceptions.FtpTransportException;
import com.okeandra.demo.models.Item;
import com.okeandra.demo.models.WarehouseItemCount;
import com.okeandra.demo.services.parsers.ExcelParser;
import com.okeandra.demo.services.transport.impl.FtpTransporterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InsalesFeed implements Processing {
    private FtpTransporterImpl ftp;
    //    private FtpDownLoader ftpDownLoader = new FtpDownLoader();
    private ExcelWarehouseLimiter excelWarehouseLimiter;

    private List<String> resultText = new ArrayList<>();

    private List<String> vendorLimitsExceptions = getVendorExceptions();

    @Value("${ftp.xls.source.file}")
    private String xlsSourceFile;

    @Value("${ftp.xls.final.destination}")
    private String xlsDownloadedFile;

    @Value("${stock.limit.okeandra}")
    private int warehouseDangerLimiter;

    @Value("${ftp.xls.final.file}")
    private String xlsResultFile;

    @Value("${ftp.okeandra.destination.directory}")
    private String ftpDestinationDirectory;

    public InsalesFeed(FtpTransporterImpl ftp, ExcelWarehouseLimiter excelWarehouseLimiter) {
        this.ftp = ftp;
        this.excelWarehouseLimiter = excelWarehouseLimiter;
    }

/*
1. Скачать XLS с FTP (1CItems.xls)
2. Лимиты: на складе Площади обнулить остатки у товаров с кол-вом < LIMIT(3). Исключения: vendor = <Kapous, Ollin, Duty free>
3. Выгрузить обработанный XLS на FTP. В админке не забыть поменять ссылку */

    @Override
    public List<String> start() {
        //Скачиваем XLS c FTP и кладем в заданную в настройках папку
        boolean isXlsSourceCopied = false;
        boolean isDangerLimitFixed = false;
        boolean isResultFileUploaded = false;
        try {
            isXlsSourceCopied = ftp.copyFileFromFtp(xlsSourceFile);
            resultText.add("Файл " + xlsSourceFile + " скопирован с FTP");
        } catch (FtpTransportException e) {
            resultText.add("Ошибка при копировании файла " + xlsSourceFile + " с FTP. Ошибка: " + e.getMessage());
        }

        //Устанавливаем лимиты в XLS (Океандра до 3 шт - обнуляем остаток, кроме DF, Kapous, Olin -> vendorLimitsExceptions;
        if (isXlsSourceCopied) {
            isDangerLimitFixed = excelWarehouseLimiter.setStockLimits(xlsDownloadedFile, warehouseDangerLimiter, vendorLimitsExceptions);
            resultText.add("Файл " + xlsDownloadedFile + " безопасный остаток зафиксирован");
        }

        //Обработанный XLS файл отправляем в FTP
        if (isDangerLimitFixed) {
            try {
//                String filenameForFtp = getFilenameFromPath(xlsResultFile);
                ftp.copyFileToFtp(ftpDestinationDirectory, xlsDownloadedFile, xlsResultFile);
                resultText.add("Файл выгрузки для Okeandra отправлен на FTP");
                isResultFileUploaded = true;
            } catch (FtpTransportException e) {
                System.out.println(e.getMessage());
            }
        }

        if (isResultFileUploaded) {
            resultText.add("Все этапы обработки успешно завершены");
        }

        return resultText;

    /*
        //Скачиваем XML фид с Insales и кладем его и кладем в заданную в настройках папку
        String localXmlDestinationFilePath = xmlSaveToPath + xmlSourceUrl.substring(xmlSourceUrl.lastIndexOf('/') + 1, xmlSourceUrl.length());
        boolean isYmlReceipted = xmlTransporter.getXmlFromUrlAndSave(xmlSourceUrl, localXmlDestinationFilePath);


        if (isXlsSourceCopied && isYmlReceipted) {
            HashMap<Item, List<WarehouseItemCount>> stock = getStockFromXls();

            // Создаем YML (заголовок, тело(List<Offer>), подвал)
            YmlObject yml = YmlObject.getYmlObject(localXmlDestinationFilePath);
            resultText.add("Парсинг YML файла прошел успешно");


            //Если хотим весь товар разделить по датам в зависимости от количества - устанавливаем в List<Offer>-ах setDate
//            ShipmentBuilder shipmentBuilderDependsOurWarehouse = new ShipmentBuilderDependsOurWarehouse(yml, stock, 1, 2);
//            shipmentBuilderDependsOurWarehouse.addShipmentOptions();
//            resultText.add("Выполнено разделение по датам в зависимости от наличия");

            //Если хотим разделить по датам только избранные товары
            List<String> idForAddingShipmentOptions = Arrays.asList(new String[]{"AUT00001915", "PLL00000572", "ТЦЦZD003517" ,"PLL00002457"});
            ShipmentBuilder shipmentBuilder = new ShipmentBuilderForSpecialItems(yml, 2, 1, idForAddingShipmentOptions);
            shipmentBuilder.addShipmentOptions();

            //Превращаем YMLObject снова в XML и сохраняем его.
            xmlFinalCreator.saveXmlFile(xmlResultFile, yml);
            resultText.add("Создан новый YML-фид");

            //Отправляем готовый фид на FTP
            try {
                String filenameForFtp = getFilenameFromPath(xmlResultFile);
                ftp.copyFileToFtp(ftpDestinationDirectory, xmlResultFile, filenameForFtp);
                resultText.add("YML-фид отправлен на FTP");
            } catch (FtpTransportException e) {
                System.out.println(e.getMessage());
            }
        }
*/

    }

    private HashMap<Item, List<WarehouseItemCount>> getStockFromXls() {
        ExcelParser excelParser = new ExcelParser();
        HashMap<Item, List<WarehouseItemCount>> stock = excelParser.getWarehouseStock(xlsDownloadedFile);
        resultText.add("Парсинг XLS файла прошел успешно");
        return stock;
    }

    private String getFilenameFromPath(String filePath) {
//        return filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());
        return filePath.substring(filePath.lastIndexOf('/') + 1);
    }

    @Autowired
    private List<String> getVendorExceptions() {
        List<String> vendorsWithoutDangerLimit = new ArrayList<>();
        // TODO get vendors from file
        vendorsWithoutDangerLimit.add("Duty Free");
        vendorsWithoutDangerLimit.add("Kapous");
        vendorsWithoutDangerLimit.add("Ollin Professional");
        return vendorsWithoutDangerLimit;
    }
}
