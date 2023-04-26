package com.hy.biz.parser.util;

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
 * @author shiwentao
 * @package com.hy.biz.parser.util
 * @description
 * @create 2023-04-25 13:25
 **/
public class GSONUtil {
    public static Gson getInstance() {
        return GsonHolder.instance;
    }

    /**
     * 1. SimpleDateFormat 为线程不安全
     * 2. 此处日期转化应对的是后台数据推送到前端时的日期格式问题(Year,Month,Day值分开), 是否需要在此处解决.
     */
    private static class GsonHolder {
        private static final Gson instance = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateTypeAdapter().nullSafe())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter().nullSafe())
                .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter().nullSafe())
                .registerTypeAdapter(Instant.class, new InstantTypeAdapter().nullSafe())
                .enableComplexMapKeySerialization()
                .addSerializationExclusionStrategy(new CustomExclusionStrategy())
                .create();

        private static class DateTypeAdapter extends TypeAdapter<Date> {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            @Override
            public void write(JsonWriter writer, Date date) throws IOException {
                writer.value(date != null ? dateFormat.format(date) : "");
            }
            @Override
            public Date read(JsonReader reader) throws IOException {
                try {
                    return dateFormat.parse(reader.nextString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        private static class ZonedDateTimeTypeAdapter extends TypeAdapter<ZonedDateTime> {
            private final String pattern = "yyyy-MM-dd HH:mm:ssX";
            @Override
            public void write(JsonWriter writer, ZonedDateTime zonedDateTime) throws IOException {
                writer.value(zonedDateTime != null ? new SimpleDateFormat(pattern).format(zonedDateTime) : "");
            }
            @Override
            public ZonedDateTime read(JsonReader reader) throws IOException {
                return ZonedDateTime.parse(reader.nextString(), DateTimeFormatter.ofPattern(pattern));
            }
        }

        private static class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {
            @Override
            public void write(JsonWriter writer, LocalDate localDate) throws IOException {
                writer.value(localDate != null ? new SimpleDateFormat("yyyy-MM-dd").format(localDate) : "");
            }
            @Override
            public LocalDate read(JsonReader reader) throws IOException {
                return LocalDate.parse(reader.nextString());
            }
        }

        private static class InstantTypeAdapter extends TypeAdapter<Instant> {
            @Override
            public void write(JsonWriter writer, Instant instant) throws IOException {
                writer.value(instant.getEpochSecond());
            }
            @Override
            public Instant read(JsonReader reader) throws IOException {
                return Instant.ofEpochSecond(reader.nextLong());
            }
        }

        private static class CustomExclusionStrategy implements ExclusionStrategy {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                Expose expose = f.getAnnotation(Expose.class);
                return (expose != null);
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        }
    }

}
