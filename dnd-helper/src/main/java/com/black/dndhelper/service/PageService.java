package com.black.dndhelper.service;

import com.black.dndhelper.dto.PeriodDto;
import com.black.dndhelper.exception.NotFoundException;
import com.black.dndhelper.model.Location;
import com.black.dndhelper.model.WeatherDay;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Занимается сборкой и отдачей текущей страницы на контроллер
 */
@Service
@RequiredArgsConstructor
public class PageService {

    private final DaoService daoService;

    public Pair<PeriodDto, List<Location>> initPage() {
        List<Location> locations = daoService.readLocations();
        PeriodDto currentPeriod = new PeriodDto(locations.get(0).getWeather().get(0).getDay(), 1);
        return new Pair<>(currentPeriod, locations);
    }

    public void processPeriod(PeriodDto curPeriod, boolean next, List<WeatherDay> weatherDays) {
        int curPeriodPeriodNumber = curPeriod.getPeriodNumber();
        String curPeriodDay = curPeriod.getDay();
        if (next) {
            if (curPeriodPeriodNumber == 6) {
                curPeriod.setPeriod(1);
                try {
                    curPeriod.setDay(getNextOrPreviousDay(weatherDays, curPeriodDay, true));
                }
                catch (NotFoundException e) {
                    e.printStackTrace();
                    curPeriod.setPeriod(curPeriodPeriodNumber);
                    curPeriod.setDay(curPeriodDay);
                }
            } else {
                curPeriod.setPeriod(curPeriodPeriodNumber + 1);
            }
        } else {
            if (curPeriodPeriodNumber == 1) {
                curPeriod.setPeriod(6);
                try {
                    curPeriod.setDay(getNextOrPreviousDay(weatherDays, curPeriodDay, false));
                }
                catch (NotFoundException e) {
                    e.printStackTrace();
                    curPeriod.setPeriod(curPeriodPeriodNumber);
                    curPeriod.setDay(curPeriodDay);
                }
            } else {
                curPeriod.setPeriod(curPeriodPeriodNumber - 1);
            }
        }
    }

    private String getNextOrPreviousDay(List<WeatherDay> weatherDays, String curDayName, boolean next) throws NotFoundException {
        int curIndex = -1;
        for (int i = 0; i < weatherDays.size(); i++) {
            if (weatherDays.get(i).getDay().equals(curDayName)) {
                curIndex = i;
                break;
            }
        }
        if (curIndex == -1) {
            throw new NotFoundException("Failed to find day by its name: " + curDayName);
        }
        int newIndex = next ? curIndex + 1 : curIndex - 1;
        if (newIndex < 0 || newIndex >= weatherDays.size()) {
            throw new NotFoundException("newIndex is beyond 1 or 6: " + newIndex);
        }
        return weatherDays.get(newIndex).getDay();
    }
}
