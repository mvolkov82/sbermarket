package com.okeandra.demo.services.processing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.okeandra.demo.exceptions.DeliveryFromSberException;
import com.okeandra.demo.exceptions.FtpTransportException;
import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.YmlObject;
import com.okeandra.demo.services.creators.XmlFinalCreator;
import com.okeandra.demo.services.transport.FromFileReader;
import com.okeandra.demo.services.transport.XmlTransporter;
import com.okeandra.demo.services.transport.impl.FtpTransporterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class DeliveryFromSberWarehouse implements Processing {

    private FtpTransporterImpl ftp;
//    private FtpDownLoader ftpDownLoader = new FtpDownLoader();
    private XmlFinalCreator xmlFinalCreator;
    private XmlTransporter xmlTransporter;
    private FromFileReader fileReader;

    @Value("${items.sberwarehouse}")
    String sberItemsFile;

    @Value("${ftp.xls.source.file}")
    String xlsSourceFile;

    @Value("${ftp.xls.final.destination}")
    String xlsDownloadedFile;

    @Value("${xml.source.url}")
    String xmlSourceUrl;

    @Value("${xml.local.destination}")
    String xmlLocalPath;

    @Value("${xml.result.file}")
    String xmlResultFile;

    @Value("${items.sberwarehouse.result.file}")
    String xmlForDeliveryFromSberWarehouse;

    @Value("${ftp.xml.destination.directory}")
    private String ftpDestinationDirectory;

    @Autowired
    public DeliveryFromSberWarehouse(FtpTransporterImpl ftp, XmlFinalCreator xmlFinalCreator, XmlTransporter xmlTransporter, FromFileReader fileReader) {
        this.ftp = ftp;
        this.xmlFinalCreator = xmlFinalCreator;
        this.xmlTransporter = xmlTransporter;
        this.fileReader = fileReader;
    }

    @Override
    public List<String> start() {
        List<String> resultText = new ArrayList<>();

        //Скачиваем XLS c FTP и кладем в заданную в настройках папку
        boolean isXlsSourceCopied = false;
        try {
            isXlsSourceCopied = ftp.copyFileFromFtp(xlsSourceFile);
            resultText.add("Файл " + xlsSourceFile + " скопирован с FTP");
        } catch (FtpTransportException e) {
            resultText.add("Ошибка при копировании файла " + xlsSourceFile + " с FTP. Ошибка: " + e.getMessage());
        }

        //Скачиваем XML фид и кладем его и кладем в заданную в настройках папку
        String localXmlDestinationFilePath = xmlLocalPath + getFilenameFromPath(xmlSourceUrl);
        boolean isYmlReceipted = xmlTransporter.getXmlFromUrlAndSave(xmlSourceUrl, localXmlDestinationFilePath);

        Set<String> sberItems;

        try {
            sberItems = fileReader.getUniqueValuesFromTextFile(sberItemsFile);
            resultText.add("Файл cо списком артикулов(" + sberItems.size() + " шт.) считан (" + sberItemsFile + ")");
        } catch (IOException e) {
            throw new DeliveryFromSberException(e.getMessage(), "Ошибка при получении списка артикулов для отгрузки со склада Сбера");
        }


        if (isXlsSourceCopied && isYmlReceipted && !sberItems.isEmpty()) {
            YmlObject yml = YmlObject.getYmlObjectOnlyForSpecialItems(localXmlDestinationFilePath);
            resultText.add("Парсинг YML файла прошел успешно");

            List<Offer> itemsResult = yml.generateOffersOnlyForSpecialItems(sberItems);
            resultText.add("Создан новый YML с товарами из файла. Товаров в фиде: " + yml.getBody().size());

            for (Offer offer:itemsResult) {
                resultText.add(offer.getVendorCode() + " " + offer.getName() + " Цена: " + offer.getPrice());
            }

            yml.dropAllStockToZero();
            resultText.add("Остаток у всех товаров обнулен (требование Сбера)");

            //Превращаем YMLObject снова в XML и сохраняем его.
            xmlFinalCreator.saveXmlFile(xmlForDeliveryFromSberWarehouse, yml);
            resultText.add("Сохранен новый YML-фид");

            try {
                String filenameForFtp = getFilenameFromPath(xmlForDeliveryFromSberWarehouse);
                ftp.copyFileToFtp(ftpDestinationDirectory, xmlForDeliveryFromSberWarehouse, filenameForFtp);
                resultText.add("YML-фид для отгрузки со склада Сбера отправлен на FTP");
            } catch (FtpTransportException e) {
                System.out.println(e.getMessage());
            }
        }
        return resultText;
    }

    private String getFilenameFromPath(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        return fileName;
    }
}
