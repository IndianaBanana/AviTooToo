package org.banana.exception;

public class SaleHistoryAdvertisementQuantityIsLowerThanExpectedException extends RuntimeException {

    private static final String MESSAGE = "Sale history advertisement quantity is lower than expected. advertisement quantity: <<%d>> expected quantity: <<%d>>";

    public SaleHistoryAdvertisementQuantityIsLowerThanExpectedException(int advertisementQuantity, int expectedQuantity) {
        super(MESSAGE.formatted(advertisementQuantity, expectedQuantity));
    }
}
