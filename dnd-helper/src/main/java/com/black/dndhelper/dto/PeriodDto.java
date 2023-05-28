package com.black.dndhelper.dto;

import lombok.Data;

@Data
public class PeriodDto {
    String day;
    String period;

    public PeriodDto(String day, int periodNumber) {
        this.day = day;
        setPeriod(periodNumber);
    }

    public void setPeriod(int number) {
        period = number + " период";
    }

    public String getFullPeriod() {
        return day + " " + period;
    }

    public int getPeriodNumber() {
        return Integer.parseInt(period.substring(0, 1));
    }
}
