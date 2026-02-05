
package com.example.lwms1.model;

import jakarta.persistence.*;

@Entity
public class Space {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer spaceId;

    private Integer totalCapacity;
    private Integer usedCapacity;
    private Integer availableCapacity;

    @Column(length = 50, unique = true)
    private String zone;

    public Space() {}
    public Integer getSpaceId() { return spaceId; }
    public void setSpaceId(Integer spaceId) { this.spaceId = spaceId; }
    public Integer getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(Integer totalCapacity) { this.totalCapacity = totalCapacity; }
    public Integer getUsedCapacity() { return usedCapacity; }
    public void setUsedCapacity(Integer usedCapacity) { this.usedCapacity = usedCapacity; }
    public Integer getAvailableCapacity() { return availableCapacity; }
    public void setAvailableCapacity(Integer availableCapacity) { this.availableCapacity = availableCapacity; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
}
