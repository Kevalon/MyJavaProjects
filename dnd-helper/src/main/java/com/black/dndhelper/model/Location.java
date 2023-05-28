package com.black.dndhelper.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Location {
    String locationName;
    List<WeatherDay> weather;
    String navigationDC;
    String plantsDC;
    String foodDC;
    String waterDC;
    String randomEncChance;
}
