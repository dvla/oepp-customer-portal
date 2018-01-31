package controllers.constants;

import models.Payment;

import static utils.URLParser.parse;

public class PaymentConstants {

    public static final Long PAYMENT_ID = 1L;
    public static final String PAYMENT_REFERENCE = "f9c9704a-83ef-471b-8f12-91a923afd3aa";
    public static final String PAYMENT_PAGE_URL = "http://localhost:999/secure-payment-form?product=Additional+info&amount=913&redirect=http://localhost/fulfill";

    public static Payment payment() {
        return new Payment(PAYMENT_ID, parse(PAYMENT_PAGE_URL));
    }
}
