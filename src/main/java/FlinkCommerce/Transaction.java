package FlinkCommerce;

import java.io.Serializable;

public class Transaction implements Serializable{

    public String transactionId;
    public String productId;
    public String name;
    public String productCategory;
    public float productPrice;
    public int productQuantity;
    public String productBrand;
    public String currency;
    public String customerId;
    public String transactionDate;
    public String paymentMethod;
    public double totalAmount;
    
    public Transaction() {}

    @Override
    public String toString() {
    return "{" +
            "transactionId='" + transactionId + '\'' +
            ", productId='" + productId + '\'' +
            ", name='" + name + '\'' +
            ", productCategory='" + productCategory + '\'' +
            ", productPrice=" + productPrice +
            ", productQuantity=" + productQuantity +
            ", productBrand='" + productBrand + '\'' +
            ", currency='" + currency + '\'' +
            ", customerId='" + customerId + '\'' +
            ", transactionDate='" + transactionDate + '\'' +
            ", paymentMethod='" + paymentMethod + '\'' +
            ", totalAmount=" + totalAmount +
            '}';
}
}