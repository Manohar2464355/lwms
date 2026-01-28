
package com.example.lwms1.dto;

import jakarta.validation.constraints.*;

public class SpaceDTO {

    private Integer spaceId;

    @NotNull @Positive
    private Integer totalCapacity;

    private Integer usedCapacity;

    @NotBlank @Size(max = 50)
    private String zone;

    public SpaceDTO() {}

    public Integer getSpaceId() { return spaceId; }
    public void setSpaceId(Integer spaceId) { this.spaceId = spaceId; }
    public Integer getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(Integer totalCapacity) { this.totalCapacity = totalCapacity; }
    public Integer getUsedCapacity() { return usedCapacity; }
    public void setUsedCapacity(Integer usedCapacity) { this.usedCapacity = usedCapacity; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
}
