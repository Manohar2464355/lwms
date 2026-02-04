package com.example.lwms1.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class ShipmentDTO {

    private Integer shipmentId;

    @NotNull(message = "You must select an item to ship")
    private Integer itemId;

    @NotBlank(message = "Origin location is required")
    @Size(max = 100)
    private String origin;

    @NotBlank(message = "Destination is required")
    @Size(max = 100)
    private String destination;

    @NotBlank(message = "Shipment status is required")
    private String status = "PENDING"; // Default status

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Must ship at least 1 item")
    private Integer quantity;

    @NotNull(message = "Delivery date is required")
    @FutureOrPresent(message = "Delivery date cannot be in the past")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedDeliveryDate;

    public ShipmentDTO() {}

    // Getters and Setters
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

    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}