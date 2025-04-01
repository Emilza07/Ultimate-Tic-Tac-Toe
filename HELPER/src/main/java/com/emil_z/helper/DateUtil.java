package com.emil_z.helper;

import android.annotation.SuppressLint;
import android.os.Build;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Date;

public class DateUtil {

    public static LocalDate stringToLocalDate(String date){
        return DateUtil.stringToLocalDate(date, "d/M/uuuu" );
        // java 8 "d/M/yyyy" => "d/M/uuuu"
    }

    public static LocalDate stringToLocalDate(String date, String datePattern){
        LocalDate localDate = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter f =  DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern)
                .withChronology(IsoChronology.INSTANCE)
                    .withResolverStyle(ResolverStyle.STRICT);

            try {
                localDate = LocalDate.parse(date, formatter);
            }
            catch (Exception e){
            }
        }

        return localDate;
    }

    public static String locaDateToString(LocalDate date){
        return DateUtil.locaDateToString(date, "dd/MM/uuuu");
    }

    public static String locaDateToString(LocalDate date, String datePattern){
        String dateStr = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter f =  DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern)
                    .withChronology(IsoChronology.INSTANCE)
                    .withResolverStyle(ResolverStyle.STRICT);

            try {
                dateStr = date.format(formatter);
            }
            catch (Exception e){
            }
        }

        return dateStr;
    }

    public static long stringDateToLong(String date){
        return DateUtil.localDateToLong(DateUtil.stringToLocalDate(date));
    }

    public static String longDateToString(long date){
        return DateUtil.locaDateToString(DateUtil.longToLocalDate(date));
    }

    @SuppressLint("NewApi")
    public static long localDateToLong(LocalDate date){
            // Specify the time zone (e.g., UTC)
            ZoneId zoneId = ZoneId.systemDefault(); // Or use ZoneId.of("UTC")

            // Convert LocalDate to LocalDateTime at the start of the day
            LocalDateTime localDateTime = date.atStartOfDay();

            // Convert LocalDateTime to Instant using the specified time zone
            Instant instant = localDateTime.atZone(zoneId).toInstant();

            // Get the milliseconds since the Unix epoch
            return instant.toEpochMilli();
    }

    @SuppressLint("NewApi")
    public static LocalDate longToLocalDate(long millis) {
        Instant instant = Instant.ofEpochMilli(millis);
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date localDateToDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        else
            return null;
    }

    public static LocalDate dateToLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        else
            return null;
    }

    public static boolean inRange(LocalDate dateToCheck, LocalDate startRange, LocalDate endRange){
        return DateUtil.inRange (dateToCheck, startRange, endRange, true);
    }

    public static boolean inRange(LocalDate dateToCheck, LocalDate startRange, LocalDate endRange, boolean includeEdges) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (includeEdges) {
                return (dateToCheck.isEqual(startRange) || dateToCheck.isAfter(startRange)) &&
                        (dateToCheck.isEqual(endRange) || dateToCheck.isBefore(endRange));
            } else {
                return dateToCheck.isAfter(startRange) && dateToCheck.isBefore(endRange);
            }
        }
        else
            return true;
    }

    public enum Periods {YEARS, MONTHS, DAYS, YEARS_MONTHS, YEAR_MONTHS_DAYS};

    public static String age (LocalDate dateOfBirth, Periods periods){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return DateUtil.age(dateOfBirth, LocalDate.now(), periods);
        }
        else
            return "-1";
    }

    public static String age(LocalDate fromDate, LocalDate toDate, Periods periods){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Period period = Period.between(fromDate, toDate);

            switch (periods){
                case YEARS: {
                    return String.valueOf(period.getYears());
                }
                case MONTHS:{
                    return String.valueOf(period.getMonths());
                }
                case DAYS:{
                    return String.valueOf(period.getDays());
                }
                case YEAR_MONTHS_DAYS:{
                    return String.valueOf(period.getYears()) + "|" +
                            String.valueOf(period.getMonths()) + "|" +
                            String.valueOf(period.getDays());
                }
                case YEARS_MONTHS:{
                    return String.valueOf(period.getYears()) + "|" +
                            String.valueOf(period.getMonths());
                }
                default:
                    return null;
            }
        }
        return "";
    }

    public static CalendarConstraints buidCalendarConstrains(LocalDate startDate, LocalDate endDate){
        return buidCalendarConstrains(startDate, endDate, null);
    }

    public static CalendarConstraints buidCalendarConstrains(LocalDate startDate, LocalDate endDate, LocalDate openAtDate){
        long dateStart = 0;
        long dateEnd = 0;
        long openAt = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dateStart = ZonedDateTime.ofLocal(startDate.atStartOfDay(), ZoneId.systemDefault(), ZoneOffset.ofHours(0)).toInstant().toEpochMilli();
            dateEnd = ZonedDateTime.ofLocal(endDate.atStartOfDay(), ZoneId.systemDefault(), ZoneOffset.ofHours(0)).toInstant().toEpochMilli();

            if (openAtDate != null)
                openAt = ZonedDateTime.ofLocal(openAtDate.atStartOfDay(), ZoneId.systemDefault(), ZoneOffset.ofHours(0)).toInstant().toEpochMilli();
        }

        CalendarConstraints.DateValidator dateValidatorMin = DateValidatorPointForward.from(dateStart);
        CalendarConstraints.DateValidator dateValidatorMax = DateValidatorPointBackward.before(dateEnd);

        ArrayList<CalendarConstraints.DateValidator> listValidators = new ArrayList<CalendarConstraints.DateValidator>();
        listValidators.add(dateValidatorMin);
        listValidators.add(dateValidatorMax);

        CalendarConstraints.DateValidator validators = CompositeDateValidator.allOf(listValidators);

        CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();
        constraintsBuilderRange.setValidator(validators);
        constraintsBuilderRange.setStart(dateStart);
        constraintsBuilderRange.setEnd(dateEnd);

        if (openAtDate != null) {
            constraintsBuilderRange.setOpenAt(openAt);
        }
        else {
            constraintsBuilderRange.setOpenAt(dateStart);
        }

        return constraintsBuilderRange.build();
    }

 }