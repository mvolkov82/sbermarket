package com.okeandra.demo.services.shipments;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.YmlObject;

public class ShipmentBuilderForSpecialItems implements ShipmentBuilder {
    private YmlObject yml;
    private int daysForOrdersInTime;
    private String lastTimeForOrder;
    private Collection<String> specialItemsIds;

    public ShipmentBuilderForSpecialItems(YmlObject yml, int daysForOrdersInTime, String lastTimeForOrder, Collection<String> specialItemsIds) {
        this.yml = yml;
        this.daysForOrdersInTime = daysForOrdersInTime;
        this.lastTimeForOrder = lastTimeForOrder;
        this.specialItemsIds = specialItemsIds;
    }

    @Override
    public void addShipmentOptions() {
        setShipmentsForSpecialItems();
    }

    private void setShipmentsForSpecialItems() {
        List<Offer> offers = yml.getBody();

        for (String itemId : specialItemsIds) {
            Optional<Offer> neededOffer = offers.stream()
                    .filter(o -> o.getVendorCode().equals(itemId))
                    .findFirst();
            neededOffer.ifPresent(offer -> offer.setDays(daysForOrdersInTime));
            neededOffer.ifPresent(offer -> offer.setOrderBefore(lastTimeForOrder));

            if (itemId.equals("PLL00000578")){
                System.out.println("PLL00000578");
            }
        }
    }

//    private void setShipmentsDefault(YmlObject yml, int defaultDays) {
//        List<Offer> offers = yml.getBody();
//        offers.forEach(x -> x.setDays(defaultDays));
//    }
}
