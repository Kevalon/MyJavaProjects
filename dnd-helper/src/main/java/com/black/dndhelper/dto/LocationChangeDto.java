package com.black.dndhelper.dto;

import com.black.dndhelper.model.WeatherDay;
import lombok.Data;

import java.util.List;

@Data
public class LocationChangeDto {
    private List<WeatherDay> weatherDayList;
    private String randomEncounterChance;
    private String waterDC;
    private String foodDC;
    private String plantsDC;
    private String navDC;
}
