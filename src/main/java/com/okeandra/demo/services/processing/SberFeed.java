package com.okeandra.demo.services.processing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.okeandra.demo.exceptions.DeliveryFromSberException;
import com.okeandra.demo.exceptions.FtpTransportException;
import com.okeandra.demo.models.Item;
import com.okeandra.demo.models.Warehouse;
import com.okeandra.demo.models.WarehouseItemCount;
import com.okeandra.demo.models.YmlObject;
import com.okeandra.demo.services.creators.XmlFinalCreator;
import com.okeandra.demo.services.parsers.ExcelParser;
import com.okeandra.demo.services.shipments.ShipmentBuilderForSpecialItems;
import com.okeandra.demo.services.transport.FromFileReader;
import com.okeandra.demo.services.transport.impl.FtpTransporterImpl;
import com.okeandra.demo.services.transport.impl.XmlTransporterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SberFeed implements Processing {
    private XmlTransporterImpl xmlTransporter;
    private FromFileReader fileReader;
    private FtpTransporterImpl ftp;
    private XmlFinalCreator xmlFinalCreator;

    @Autowired
    public SberFeed(XmlTransporterImpl xmlTransporter, FromFileReader fileReader, FtpTransporterImpl ftp, XmlFinalCreator xmlFinalCreator) {
        this.xmlTransporter = xmlTransporter;
        this.fileReader = fileReader;
        this.ftp = ftp;
        this.xmlFinalCreator = xmlFinalCreator;
    }

    @Value("${xml.source.url}")
    private String xmlSourceUrl;

    @Value("${xml.local.destination}")
    private String xmlLocalPath;

    @Value("${items.dayperday}")
    private String dayPerDayFile;

    @Value("${xml.result.file}")
    private String finishedFeedForSberMarket;

    @Value("${ftp.xml.destination.directory}")
    private String ftpDestinationDirectory;

    @Value("${ftp.xls.final.destination}")
    private String xlsFilename;

    @Override
    public List<String> start() {
        List<String> resultText = new ArrayList<>();

        //Скачиваем XML фид и кладем его в заданную в настройках папку
        String localXmlDestinationFilePath = xmlLocalPath + getFilenameFromPath(xmlSourceUrl);
        //РАСКОМЕНТИРОВАТЬ ПОСЛЕ ОТЛАДКИ
        boolean isYmlReceipted = xmlTransporter.getXmlFromUrlAndSave(xmlSourceUrl, localXmlDestinationFilePath);
        //Закмоентировать после отладки
        //        boolean isYmlReceipted = true;


        Set<String> dayPerDayItems;
        try {
            dayPerDayItems = fileReader.getUniqueValuesFromTextFile(dayPerDayFile);
            resultText.add("Файл cо списком товаров день в день (" + dayPerDayItems.size() + " шт.) считан (" + dayPerDayFile + ")");
        } catch (IOException e) {
            throw new DeliveryFromSberException(e.getMessage(), "Ошибка при получении списка артикулов для отгрузки со склада Сбера");
        }

        //Обрабатываем YML -
        if (isYmlReceipted) {
            YmlObject yml = YmlObject.getYmlObjectOnlyForSpecialItems(localXmlDestinationFilePath);
            resultText.add("Парсинг YML файла прошел успешно");

            //TODO
            ShipmentBuilderForSpecialItems shipmentBuilder = new ShipmentBuilderForSpecialItems(yml, 0, "01:00", dayPerDayItems);
            shipmentBuilder.addShipmentOptions();
            resultText.add("Настроена доставка день в день на спец товары");

            //Превращаем YMLObject снова в XML и сохраняем его.
            xmlFinalCreator.saveXmlFile(finishedFeedForSberMarket, yml);
            resultText.add("Сохранен новый YML-фид: " + finishedFeedForSberMarket);

            try {
                String filenameForFtp = finishedFeedForSberMarket.substring(finishedFeedForSberMarket.lastIndexOf('\\') + 1);
                ftp.copyFileToFtp(ftpDestinationDirectory, finishedFeedForSberMarket, filenameForFtp);
                resultText.add("YML-фид для отгрузки со склада Сбера отправлен на FTP");
            } catch (FtpTransportException e) {
                System.out.println(e.getMessage());
                resultText.add("Ошибка при отправке фида на FTP: " + e.getMessage());
            }
        }

        return resultText;
    }

    private String getFilenameFromPath(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        return fileName;
    }
}
