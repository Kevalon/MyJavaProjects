package com.black.dndhelper.dto;

import com.black.dndhelper.model.Location;
import lombok.Data;

import java.util.List;

/**
 * Хранит в себе все передаваемые на html поля
 */
@Data
public class PageDto {
    List<Location> locations;
}
