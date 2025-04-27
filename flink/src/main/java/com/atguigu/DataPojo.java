package com.atguigu;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;

@JsonPropertyOrder({"city","lat","lng","country","iso2",
        "adminName","capital","population"})
public class DataPojo {
    public String city;
    public BigDecimal lat;
    public BigDecimal lng;
    public String country;
    public String iso2;
    public String adminName;
    public String capital;
    public long population;

    @Override
    public String toString() {
        return super.toString();
    }

    public String getCity() {
        return city;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public BigDecimal getLng() {
        return lng;
    }

    public String getCountry() {
        return country;
    }

    public String getIso2() {
        return iso2;
    }

    public String getAdminName() {
        return adminName;
    }

    public String getCapital() {
        return capital;
    }

    public long getPopulation() {
        return population;
    }
}
