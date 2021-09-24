package com.okeandra.demo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/* Уточнить актуальность действий
1. Скачать XLS с FTP (1CItems.xls)
2. Скачать feed с Insales
3. Лимиты: на складе Площади обнулить остатки у товаров с кол-вом < LIMIT(3). Исключения: vendor = Kapous, Ollin, Duty free
4. Установить shipment-options = 1D для товаров из списка
5. Создать новый фид и выгрузить его на FTP
6. СберМегаМаркету дать ссылку на п 5.
 */

@SpringBootApplication
public class ApplicationStarter {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationStarter.class, args);
//        ProcessingController processingController = new ProcessingController();
//        demoStarter.start();


    }
    /*  Тушь XXL СУПЕРОБЪЕМ эффект НАКЛАДНЫХ РЕСНИЦ 8гр/К10	AUT00001915
        Belita Young ВВ крем для лица 30мл	PLL00000572
        LUXURY Тушь для ресниц Королевский объем 12мл	ТЦЦZD003517
        Гель-фиксатор для бровей "Brow fixing" К6	PLL00002457

    }*/
}

