package com.raf.sk.specification.model.time;

import com.raf.sk.specification.model.Day;

/**
 * Interface for time.
 *
 * @param <D> the date type
 */
public interface Time<D> {

    Day getDay();

    void setDay(Day day);

    String getStartTime();

    void setStartTime(String startTime);

    String getEndTime();

    void setEndTime(String endTime);

    D getStartDate();

    void setStartDate(D startDate);

    D getEndDate();

    void setEndDate(D endDate);

    D getDate();

    void setDate(D date);

}
