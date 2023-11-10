package com.raf.sk.specification.model.adapter;

import com.google.gson.*;
import com.raf.sk.specification.model.Day;
import com.raf.sk.specification.model.time.ReservedTime;
import com.raf.sk.specification.model.time.Time;

import java.lang.reflect.Type;
import java.time.LocalDate;

public class TimeAdapter implements JsonSerializer<Time<LocalDate>>, JsonDeserializer<Time<LocalDate>> {

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

    @Override
    public Time<LocalDate> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String day = jsonObject.get("day").getAsString();
        String startTime = jsonObject.get("startTime").getAsString();
        String endTime = jsonObject.get("endTime").getAsString();
        String startDate = jsonObject.get("startDate").getAsString();
        String endDate = jsonObject.get("endDate").getAsString();
        return new ReservedTime(Day.valueOf(day), startTime, endTime, LocalDate.parse(startDate), LocalDate.parse(endDate));
    }

}