package com.raf.sk.specification.model.time;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDate;

public class TimeAdapter implements JsonSerializer<Time<LocalDate>> {

    @Override
    public JsonElement serialize(Time<LocalDate> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("day", src.getDay().toString());
        jsonObject.addProperty("startTime", src.getStartTime());
        jsonObject.addProperty("endTime", src.getEndTime());
        jsonObject.addProperty("startDate", src.getStartDate().toString());
        jsonObject.addProperty("endDate", src.getEndDate().toString());
        return jsonObject;
    }

}