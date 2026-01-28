
package com.example.lwms1.dto;

import jakarta.validation.constraints.*;

public class InventoryDTO {

    private Integer itemId;

    @NotBlank @Size(max = 100)
    private String itemName;

    @Size(max = 50)
    private String category;

    @NotNull @PositiveOrZero
    private Integer quantity;

    @Size(max = 100)
    private String location;

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
}
