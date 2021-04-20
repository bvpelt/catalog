package com.bsoft.catalogus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcesResult {
    private int entries;
    private int pages;
    private int status;
    private String message;

    private boolean more;
}
