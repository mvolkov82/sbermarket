package com.okeandra.demo.services.creators;

import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.YmlObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component
@Qualifier("xmlCreatorGroupPrice")
public class XmlCreatorForGroupPrice extends XmlCreator {
    @Override
    public String getBodyAsXML(YmlObject ymlObject) {
        Document document = getDocument(ymlObject);

        Element root = document.createElement("offers");
        document.appendChild(root);

        for (Offer offer : ymlObject.getBody()) {
            Element blockOffer = document.createElement("offer");
            blockOffer.setAttribute("available", "true");
            blockOffer.setAttribute("id", String.valueOf(offer.getId()));
            root.appendChild(blockOffer);

            Element url = document.createElement("url");
            url.appendChild(document.createTextNode(offer.getUrl()));
            blockOffer.appendChild(url);

            Element price = document.createElement("price");
            int priceValue = (int) offer.getPrice();
            price.appendChild(document.createTextNode(String.valueOf(priceValue)));
            blockOffer.appendChild(price);

            Element categoryId = document.createElement("categoryId");
            categoryId.appendChild(document.createTextNode(offer.getCategoryId()));
            blockOffer.appendChild(categoryId);

            Element picture = document.createElement("picture");
            picture.appendChild(document.createTextNode(offer.getPicture()));
            blockOffer.appendChild(picture);

            Element name = document.createElement("name");
            name.appendChild(document.createTextNode(offer.getName()));
            blockOffer.appendChild(name);

            Element vendor = document.createElement("vendor");
            String vendorText = getTextFromValue(offer.getVendor());
            vendor.appendChild(document.createTextNode(vendorText));
            blockOffer.appendChild(vendor);

            Element vendorCode = document.createElement("vendorCode");
            vendorCode.appendChild(document.createTextNode(offer.getVendorCode()));
            blockOffer.appendChild(vendorCode);

            Element barcode = document.createElement("barcode");
            String barcodeValue = getTextFromValue(offer.getBarcode());
            barcode.appendChild(document.createTextNode(barcodeValue));
            blockOffer.appendChild(barcode);

            Element description = document.createElement("description");
            String descriptionText = getTextFromValue(offer.getDescription());
            description.appendChild(document.createTextNode(descriptionText));
            blockOffer.appendChild(description);

            Element mainCategory = document.createElement("main-category");
            String mainCategoryValue = getTextFromValue(offer.getRootCategory());
            mainCategory.appendChild(document.createTextNode(mainCategoryValue));
            blockOffer.appendChild(mainCategory);

            Element naznachenie = document.createElement("destination");
            String naznachenieValue = getTextFromValue(offer.getNaznachenie());
            naznachenie.appendChild(document.createTextNode(naznachenieValue));
            blockOffer.appendChild(naznachenie);

            Element kindProduct = document.createElement("kind-product");
            String kindProductValue = getTextFromValue(offer.getVidProduc());
            kindProduct.appendChild(document.createTextNode(kindProductValue));
            blockOffer.appendChild(kindProduct);

            Element outlets = document.createElement("outlets");
            blockOffer.appendChild(outlets);

            Element outlet = document.createElement("outlet");
            outlet.setAttribute("id", offer.getOutletId());
            outlet.setAttribute("instock", String.valueOf(offer.getInStock()));
            outlets.appendChild(outlet);


            //<shipment-options> <option days="1" order-before="15:00"/> </shipment-options>
            Element shipmentOptions = document.createElement("shipment-options");
            blockOffer.appendChild(shipmentOptions);
            Element shipmentOptionElement = document.createElement("option");
            shipmentOptionElement.setAttribute("order-before", "07:00");
            if (offer.getDays() == null) {
                shipmentOptionElement.setAttribute("days", String.valueOf(1));
            } else {
                shipmentOptionElement.setAttribute("days", String.valueOf(offer.getDays()));
            }
            shipmentOptions.appendChild(shipmentOptionElement);
        }

        return transformBodyToString(document);

    }


}
