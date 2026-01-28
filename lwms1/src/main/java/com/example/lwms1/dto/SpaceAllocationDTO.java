
package com.example.lwms1.dto;

import jakarta.validation.constraints.*;

public class SpaceAllocationDTO {

    @NotNull @Positive
    private Integer amount;

    public SpaceAllocationDTO() {}

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
}
