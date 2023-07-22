package com.sendByOP.expedition.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUse {

    private Date date1;
    private Date date2;


    public DateUse(Date date1, Date date2) {
        this.date1 = date1;
        this.date2 = date2;
    }

    public DateUse() {
    }

    public static long diff(Date dateE, Date dateO) {
        return dateE.getTime() - dateO.getTime();
    }

    public static Date calculateExpiryDate(final int expiryTimeInMinutes) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    public Date getDate1() {
        return date1;
    }

    public void setDate1(Date date1) {
        this.date1 = date1;
    }

    public Date getDate2() {
        return date2;
    }

    public void setDate2(Date date2) {
        this.date2 = date2;
    }
}
