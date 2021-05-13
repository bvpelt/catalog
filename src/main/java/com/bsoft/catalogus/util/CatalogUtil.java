package com.bsoft.catalogus.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CatalogUtil {

    public String getCurrentDate() {
        String date;

        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zoneId);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        date = dateFormatter.format(today);

        return date;
    }
}
