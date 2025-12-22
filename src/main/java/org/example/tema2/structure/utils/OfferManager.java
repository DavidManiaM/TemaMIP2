package org.example.tema2.structure.utils;

import java.util.HashMap;
import java.util.Map;

public class OfferManager {
    private static OfferManager instance;
    private Map<String, Boolean> offerStatus;

    public static final String HAPPY_HOUR_DRINKS = "Happy Hour Drinks";
    public static final String MEAL_DEAL = "Meal Deal";
    public static final String PARTY_PACK = "Party Pack";

    private OfferManager() {
        offerStatus = new HashMap<>();
        offerStatus.put(HAPPY_HOUR_DRINKS, false);
        offerStatus.put(MEAL_DEAL, false);
        offerStatus.put(PARTY_PACK, false);
    }

    public static OfferManager getInstance() {
        if (instance == null) {
            instance = new OfferManager();
        }
        return instance;
    }

    public void setOfferStatus(String offerName, boolean isActive) {
        offerStatus.put(offerName, isActive);
    }

    public boolean isOfferActive(String offerName) {
        return offerStatus.getOrDefault(offerName, false);
    }

    public Map<String, Boolean> getOfferStatus() {
        return offerStatus;
    }
}

