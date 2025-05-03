package de.plimplom.addonreader.lua;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import party.iroiro.luajava.Lua;
import party.iroiro.luajava.lua54.Lua54;
import party.iroiro.luajava.value.LuaTableValue;
import party.iroiro.luajava.value.LuaValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TableParser {
    private final static Gson gson = new GsonBuilder().create();

    public static JsonElement parseLuaTableToJsonNative(InputStream input) throws IOException {
        String result;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            result = reader.lines().collect(Collectors.joining("\n"));
        }
        if (result.contains("function") || !result.contains("GambleStats")) {
            return new JsonObject();
        }
        try (Lua lua = new Lua54()) {
            lua.openLibraries();

            lua.run(result);

            LuaValue val = lua.eval("return GambleStats")[0];

            if (val instanceof LuaTableValue table) {
                return gson.toJsonTree(mapLuaTable(table));
            } else {
                return new JsonObject();
            }
        }
    }

    private static Map<String, Object> mapLuaTable(LuaTableValue table) {
        var map = new HashMap<String, Object>();
        for (LuaValue key : table.keySet()) {
            var val = table.get(key);
            if (val instanceof LuaTableValue subTable) {
                map.put(key.toString(), mapLuaTable(subTable));
            } else {
                map.put(key.toString(), mapLuaValue(val));
            }
        }
        return map;
    }

    private static Object mapLuaValue(LuaValue val) {
        return switch (val.type()) {
            case NUMBER -> val.toInteger();
            case BOOLEAN -> val.toBoolean();
            default -> val.toString();
        };
    }
}
