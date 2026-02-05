package com.example.lwms1.model;

import com.example.lwms1.exception.BusinessException;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId;
    @ManyToOne
    @JoinColumn(name = "space_id")
    private Space storageSpace;

    @Column(length = 100, nullable = false)
    private String itemName;

    @Column(length = 50)
    private String category;

    private Integer quantity;

    @Column(length = 100)
    private String location;

    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    public Inventory() {}

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

    public Space getStorageSpace() {
        return storageSpace;
    }


    public void setStorageSpace(Space storageSpace) {
        this.storageSpace = storageSpace;
    }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}