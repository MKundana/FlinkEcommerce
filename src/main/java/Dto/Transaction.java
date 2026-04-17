package Dto;

import lombok.Data;

import java.sql.Timestamp;

public class Transaction{

    private String transactionId;
    private String productId;
    private String name;
    private String productCategory;
    private double productPrice;
    private int productQuantity;
    private String productBrand;
    private double currency;
    private String customerId;
    private Timestamp transactionDate;
    private String paymentMethod;
    private double totalAmount;
}