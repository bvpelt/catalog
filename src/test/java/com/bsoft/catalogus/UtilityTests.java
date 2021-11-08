package com.bsoft.catalogus;

import com.bsoft.catalogus.util.CatalogUtil;
import com.bsoft.catalogus.util.StringChanged;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.util.Assert;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.boot.test.context.SpringBootTest;

import javax.print.DocFlavor;
import java.util.Optional;

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
        Optional<String> osnull = Optional.ofNullable(snull);
        Optional<String> os1 = Optional.of(s1);
        Optional<String> os2 = Optional.of(s2);

        JsonNullable<String> ojsnull = JsonNullable.of(snull);
        JsonNullable<String> ojs1 = JsonNullable.of(s1);
        JsonNullable<String> ojs2 = JsonNullable.of(s2);

        boolean result;

        result = StringChanged.stringChanged(snull, snull);
        Assert.equals(false, result);

        result = StringChanged.stringChanged(snull, s1);
        Assert.equals(true, result);

        result = StringChanged.stringChanged(s1, snull);
        Assert.equals(true, result);

        result = StringChanged.stringChanged(s1, s2);
        Assert.equals(true, result);

        // Optional
        result = StringChanged.stringChanged(osnull, snull);
        Assert.equals(false, result);

        result = StringChanged.stringChanged(osnull, s1);
        Assert.equals(true, result);

        result = StringChanged.stringChanged(os1, snull);
        Assert.equals(true, result);

        result = StringChanged.stringChanged(os1, os2);
        Assert.equals(true, result);

        // JSonnullable
        result = StringChanged.stringChanged(ojsnull, snull);
        Assert.equals(false, result);

        result = StringChanged.stringChanged(ojsnull, s1);
        Assert.equals(true, result);

        result = StringChanged.stringChanged(ojs1, snull);
        Assert.equals(true, result);

        result = StringChanged.stringChanged(ojs1, ojs2);
        Assert.equals(true, result);
    }
}
