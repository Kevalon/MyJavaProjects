package com.black.dndhelper.controller;

import com.black.dndhelper.dto.LocationChangeDto;
import com.black.dndhelper.dto.PeriodDto;
import com.black.dndhelper.exception.NotFoundException;
import com.black.dndhelper.model.Location;
import com.black.dndhelper.service.PageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Управляет изменением всех действий пользователя.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping
public class PageController {

    private final PageService pageService;

    private PeriodDto currentPeriod;
    private List<Location> locations;
    private int currentLocationId = 0;

    @GetMapping
    public String mainPage(Model model) {
        Pair<PeriodDto, List<Location>> pair = pageService.initPage();
        currentPeriod = pair.getFirst();
        locations = pair.getSecond();
        Location curLocation = locations.get(currentLocationId);
        model.addAttribute("navDC", curLocation.getNavigationDC());
        model.addAttribute("plantsDC", curLocation.getPlantsDC());
        model.addAttribute("foodDC", curLocation.getFoodDC());
        model.addAttribute("waterDC", curLocation.getWaterDC());
        model.addAttribute("randomEncounterChance", curLocation.getRandomEncChance());
        model.addAttribute("locationNames", locations.stream().map(Location::getLocationName).toList());
        model.addAttribute("curLocationName", curLocation.getLocationName());
        model.addAttribute("curDayAndWatch", currentPeriod.getFullPeriod());
        model.addAttribute("weatherDayList", curLocation.getWeather());
        return "mainPage";
    }

    @GetMapping("/changePeriod")
    @ResponseBody
    public String changePeriod(@RequestParam("next") boolean next) {
        pageService.processPeriod(currentPeriod, next, locations.get(currentLocationId).getWeather());
        return currentPeriod.getFullPeriod();
    }

    @GetMapping("/changeLocation")
    @ResponseBody
    public LocationChangeDto changeLocation(@RequestParam("newLocationName") String newLocationName) {
        LocationChangeDto locationChangeDto = new LocationChangeDto();
        try {
            currentLocationId = IntStream.range(0, locations.size())
                                         .filter(i -> locations.get(i).getLocationName().equals(newLocationName))
                                         .findFirst()
                                         .orElseThrow(() -> new NotFoundException("Invalid location name"));
        }
        catch (NotFoundException e) {
            e.printStackTrace();
        }
        currentPeriod = new PeriodDto(locations.get(currentLocationId).getWeather().get(0).getDay(), 1);

        Location location = locations.get(currentLocationId);
        locationChangeDto.setFoodDC(location.getFoodDC());
        locationChangeDto.setNavDC(location.getNavigationDC());
        locationChangeDto.setPlantsDC(location.getPlantsDC());
        locationChangeDto.setRandomEncounterChance(location.getRandomEncChance());
        locationChangeDto.setWaterDC(location.getWaterDC());
        locationChangeDto.setWeatherDayList(location.getWeather());
        locationChangeDto.setCurDayAndWatch(currentPeriod.getFullPeriod());
        return locationChangeDto;
    }
}
