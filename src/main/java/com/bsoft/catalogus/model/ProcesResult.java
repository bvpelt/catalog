package com.bsoft.catalogus.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcesResult {
    public static final int SUCCESS = 0;
    public static final int ERROR = 1;

    private int entries;
    private int pages;
    private int status;
    private String message;

    private boolean more;
}
