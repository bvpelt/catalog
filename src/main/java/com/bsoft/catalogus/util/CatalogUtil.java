package com.bsoft.catalogus.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CatalogUtil {

    public String getCurrentDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date = df.format(new Date());

        return date;
    }
}
