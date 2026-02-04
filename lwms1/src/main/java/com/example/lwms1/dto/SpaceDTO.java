package com.example.lwms1.dto;

import jakarta.validation.constraints.*;

public class SpaceDTO {

    private Integer spaceId;

    @NotBlank(message = "Zone name is required (e.g., Zone A, Cold Storage)")
    @Size(max = 50, message = "Zone name must be under 50 characters")
    private String zone;

    @NotNull(message = "Total capacity must be defined")
    @Min(value = 1, message = "Total capacity must be at least 1 unit")
    private Integer totalCapacity;

    @PositiveOrZero(message = "Used capacity cannot be negative")
    private Integer usedCapacity = 0; // Default to 0 for new zones

    public SpaceDTO() {}

    // Getters and Setters
    public Integer getSpaceId() { return spaceId; }
    public void setSpaceId(Integer spaceId) { this.spaceId = spaceId; }

    public Integer getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(Integer totalCapacity) { this.totalCapacity = totalCapacity; }

    public Integer getUsedCapacity() { return usedCapacity; }
    public void setUsedCapacity(Integer usedCapacity) { this.usedCapacity = usedCapacity; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
}