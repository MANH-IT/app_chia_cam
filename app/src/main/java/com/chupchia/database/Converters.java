package com.chupchia.database;

import androidx.room.TypeConverter;

import com.chupchia.models.EditHistory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.chupchia.models.Member;
import com.chupchia.models.Group;

public class Converters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<EditHistory> fromString(String value) {
        if (value == null) return new ArrayList<>();
        Type listType = new TypeToken<ArrayList<EditHistory>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromList(List<EditHistory> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static Map<String, Integer> fromMapString(String value) {
        if (value == null) return new HashMap<>();
        Type mapType = new TypeToken<HashMap<String, Integer>>() {}.getType();
        return gson.fromJson(value, mapType);
    }

    @TypeConverter
    public static String fromMap(Map<String, Integer> map) {
        return gson.toJson(map);
    }

    @TypeConverter
    public static List<Member> fromMemberString(String value) {
        if (value == null) return new ArrayList<>();
        Type listType = new TypeToken<ArrayList<Member>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromMemberList(List<Member> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static Group.GroupSettings fromSettingsString(String value) {
        if (value == null) return new Group.GroupSettings();
        return gson.fromJson(value, Group.GroupSettings.class);
    }

    @TypeConverter
    public static String fromSettings(Group.GroupSettings settings) {
        return gson.toJson(settings);
    }
}
