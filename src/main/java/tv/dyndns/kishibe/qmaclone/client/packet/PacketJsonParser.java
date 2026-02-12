package tv.dyndns.kishibe.qmaclone.client.packet;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * Packet系のJSONデコード共通処理。
 */
final class PacketJsonParser {
  private PacketJsonParser() {
  }

  /**
   * 文字列をJSONオブジェクトへ変換する。
   */
  static JSONObject parseObject(String json) {
    JSONValue value = JSONParser.parseStrict(json);
    JSONObject object = value.isObject();
    if (object == null) {
      throw new IllegalArgumentException("JSON object expected");
    }
    return object;
  }

  static JSONObject getObject(JSONObject object, String key) {
    JSONValue value = object.get(key);
    return value == null ? null : value.isObject();
  }

  static JSONArray getArray(JSONObject object, String key) {
    JSONValue value = object.get(key);
    return value == null ? null : value.isArray();
  }

  static String getString(JSONObject object, String key) {
    JSONValue value = object.get(key);
    if (value == null || value.isNull() != null) {
      return null;
    }
    JSONString jsonString = value.isString();
    if (jsonString != null) {
      return jsonString.stringValue();
    }
    return null;
  }

  static int getInt(JSONObject object, String key) {
    JSONValue value = object.get(key);
    if (value == null || value.isNull() != null) {
      return 0;
    }
    JSONNumber number = value.isNumber();
    if (number != null) {
      return (int) number.doubleValue();
    }
    JSONString string = value.isString();
    if (string != null) {
      return Integer.parseInt(string.stringValue());
    }
    return 0;
  }

  static long getLong(JSONObject object, String key) {
    JSONValue value = object.get(key);
    if (value == null || value.isNull() != null) {
      return 0L;
    }
    JSONNumber number = value.isNumber();
    if (number != null) {
      return (long) number.doubleValue();
    }
    JSONString string = value.isString();
    if (string != null) {
      return Long.parseLong(string.stringValue());
    }
    return 0L;
  }

  static boolean getBoolean(JSONObject object, String key) {
    JSONValue value = object.get(key);
    if (value == null || value.isNull() != null) {
      return false;
    }
    JSONBoolean jsonBoolean = value.isBoolean();
    if (jsonBoolean != null) {
      return jsonBoolean.booleanValue();
    }
    JSONString string = value.isString();
    if (string != null) {
      return Boolean.parseBoolean(string.stringValue());
    }
    return false;
  }
}
