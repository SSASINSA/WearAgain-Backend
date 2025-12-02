package com.ssasinsa.wearagain.domain.auth.entity;

public enum AdminSignupRequestKeywordScope {
    ALL,
    EMAIL,
    NAME;

    public boolean includesEmail() {
        return this == ALL || this == EMAIL;
    }

    public boolean includesName() {
        return this == ALL || this == NAME;
    }
}
