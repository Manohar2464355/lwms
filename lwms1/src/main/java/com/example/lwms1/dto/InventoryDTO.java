package com.example.lwms1.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class InventoryDTO {

    private Integer itemId;

    @NotBlank(message = "Item name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String itemName;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category name is too long")
    private String category;

    @NotNull(message = "Quantity cannot be empty")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotBlank(message = "Storage location/zone is required")
    private String location;

    private LocalDateTime lastUpdated;

    public InventoryDTO() {}

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}