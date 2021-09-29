package com.bsoft.catalogus.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CrawlResult {
    private ProcesResult conceptSchema;
    private ProcesResult concepten;
    private ProcesResult collecties;
    private ProcesResult bronnen;
    private ProcesResult waarden;
    private String message;
    private int status;
}
