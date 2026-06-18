package com.velsis.speedviolation.domain.model;

public enum Severity {

    MEDIUM("218-I"),
    SERIOUS("218-II"),
    VERY_SERIOUS("218-III");

    private final String ctbCode;

    Severity(String ctbCode) {
        this.ctbCode = ctbCode;
    }

    public String ctbCode() {
        return ctbCode;
    }
}
