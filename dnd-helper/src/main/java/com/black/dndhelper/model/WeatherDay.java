package com.black.dndhelper.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Содержит погоду, маппится на таблицу
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeatherDay {

    Long lineNumber;
    String day;
    String temperature;
    String precipitation;
    String wind;
    String sky;
}
