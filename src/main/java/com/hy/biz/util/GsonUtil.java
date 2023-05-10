package com.hy.biz.util;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 单例GSON对象
 */
public class GsonUtil {
    public static Gson getInstance() {
        return GsonHolder.sInstance;
    }

    //TODO GsonHolder 类中实现的日期转换方法. 20180226

    /**
     * 1. SimpleDateFormat 为线程不安全
     * 2. 此处日期转化应对的是后台数据推送到前端时的日期格式问题(Year,Month,Day值分开), 是否需要在此处解决.
     */

    private static class GsonHolder {
        private static final Gson sInstance = new GsonBuilder()
                .registerTypeAdapter(Date.class, new TypeAdapter<Date>() {

                    @Override
                    public void write(JsonWriter out, Date value) throws IOException {
                        out.value(value != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value) : "");
                    }

                    @Override
                    public Date read(JsonReader in) throws IOException {
                        try {
                            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(in.nextString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.nullSafe())
                .registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
                    @Override
                    public void write(JsonWriter out, ZonedDateTime value) throws IOException {
                        out.value(value != null ? value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX")) : "");
                    }

                    @Override
                    public ZonedDateTime read(JsonReader in) throws IOException {
                        return ZonedDateTime.parse(in.nextString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX"));
                    }
                }.nullSafe())
                .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                    @Override
                    public void write(JsonWriter out, LocalDate value) throws IOException {
                        out.value(value != null ? value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
                    }

                    @Override
                    public LocalDate read(JsonReader in) throws IOException {
                        return LocalDate.parse(in.nextString());
                    }
                }.nullSafe())
                .registerTypeAdapter(Instant.class, new TypeAdapter<Instant>() {
                    @Override
                    public void write(JsonWriter out, Instant value) throws IOException {
                        //对象 转为 JSON字符串
                        out.value(value.getEpochSecond());
                    }

                    @Override
                    public Instant read(JsonReader in) throws IOException {
                        //JSON字符串 转为 对象
                        return Instant.ofEpochSecond(in.nextLong());
                    }
                }.nullSafe())
                .enableComplexMapKeySerialization()
                .addSerializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        Expose expose = f.getAnnotation(Expose.class);
                        return (expose != null);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();

    }
}
