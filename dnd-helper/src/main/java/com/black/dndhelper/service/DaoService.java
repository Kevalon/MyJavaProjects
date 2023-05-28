package com.black.dndhelper.service;

import com.black.dndhelper.model.Location;
import com.black.dndhelper.model.WeatherDay;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Занимается вытаскиванием и маппингом данных из xml в жабу.
 */
@Service
@RequiredArgsConstructor
public class DaoService {

    private static final String PATH =
            "C:\\Users\\vbifu\\OneDrive\\Рабочий стол\\Important stuff\\Тип важное\\D&D\\5 EDITION\\West Marches\\dao\\weather.xlsx";

    public List<Location> readLocations() {
        try (FileInputStream inputStream = new FileInputStream(PATH)) {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            List<Location> res = new ArrayList<>();

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                XSSFSheet sheet = workbook.getSheetAt(i);
                Location location = new Location();
                location.setLocationName(sheet.getSheetName());
                XSSFRow rowTwo = sheet.getRow(1);
                location.setNavigationDC(String.valueOf((int) rowTwo.getCell(CellReference.convertColStringToIndex("G")).getNumericCellValue()));
                location.setPlantsDC(String.valueOf((int) rowTwo.getCell(CellReference.convertColStringToIndex("H")).getNumericCellValue()));
                location.setFoodDC(String.valueOf((int) rowTwo.getCell(CellReference.convertColStringToIndex("I")).getNumericCellValue()));
                location.setWaterDC(String.valueOf((int) rowTwo.getCell(CellReference.convertColStringToIndex("J")).getNumericCellValue()));
                location.setRandomEncChance(sheet.getRow(3).getCell(CellReference.convertColStringToIndex("G")).getStringCellValue());
                location.setWeather(readWeather(sheet));
                res.add(location);
            }
            return res;
        }
        catch (IOException exception) {
            System.out.println("Failed to open the weather");
            return Collections.emptyList();
        }
    }

    private List<WeatherDay> readWeather(XSSFSheet sheet) {
        int rowNum = 1;
        XSSFRow currentRow = sheet.getRow(rowNum);
        List<WeatherDay> res = new ArrayList<>();
        while (currentRow != null) {
            WeatherDay day = new WeatherDay();
            day.setLineNumber((long) (rowNum + 1));
            day.setDay(currentRow.getCell(0).getStringCellValue());
            day.setTemperature(currentRow.getCell(1).getStringCellValue());
            day.setPrecipitation(currentRow.getCell(2).getStringCellValue());
            day.setWind(currentRow.getCell(3).getStringCellValue());
            day.setSky(currentRow.getCell(4).getStringCellValue());

            res.add(day);
            rowNum++;
            currentRow = sheet.getRow(rowNum);
        }
        return res;
    }
}
