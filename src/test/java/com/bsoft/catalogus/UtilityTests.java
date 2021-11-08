package com.bsoft.catalogus;

import com.bsoft.catalogus.util.CatalogUtil;
import com.bsoft.catalogus.util.StringChanged;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.util.Assert;
import org.springframework.boot.test.context.SpringBootTest;

import javax.print.DocFlavor;

@Slf4j
@SpringBootTest
public class UtilityTests {

    @Test
    public void getDate() {
        CatalogUtil ct = new CatalogUtil();
        String date = ct.getCurrentDate();
        log.info("Current data: {}", date);
    }

    @Test void stringChangedTest() {
        String snull = null;
        String s1 = "s1";
        String s2 = "s2";

        boolean result;

        result = StringChanged.stringChanged(snull, snull);
        Assert.equals(false, result);

        result = StringChanged.stringChanged(snull, s1);
        Assert.equals(true, result);

        result = StringChanged.stringChanged(s1, snull);
        Assert.equals(true, result);

        result = StringChanged.stringChanged(s1, s2);
        Assert.equals(true, result);
    }
}
