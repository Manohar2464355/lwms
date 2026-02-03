
package com.example.lwms1.dto;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

public class ShipmentDTO {
    private Integer shipmentId;
    @NotNull private Integer itemId;
    @NotBlank private String origin;
    @NotBlank private String destination;
    private String status;
    @Min(value = 1, message = "Must ship at least 1 item")
    private Integer quantity;


    @FutureOrPresent(message = "Delivery date cannot be in the past")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate expectedDeliveryDate;

    // Standard Getters and Setters
    public Integer getShipmentId() { return shipmentId; }
    public void setShipmentId(Integer shipmentId) { this.shipmentId = shipmentId; }
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public java.time.LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(java.time.LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
