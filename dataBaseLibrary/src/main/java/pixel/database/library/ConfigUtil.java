package pixel.database.library;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by panxi on 2016/4/22.
 */
public class ConfigUtil {
    private static final String NAME = ConfigUtil.class.getName();
    private static SharedPreferences preferences = null;
    private static SharedPreferences.Editor editor = null;

    /**
     * 获取配置对象
     */
    public static SharedPreferences getPreferences(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(NAME.replace(".", "_") + ".config", Context.MODE_PRIVATE);
        }
        return preferences;
    }

    /**
     * 获取配置编辑对象
     */
    public static SharedPreferences.Editor getEditor(Context context) {
        if (editor == null) {
            editor = getPreferences(context).edit();
        }
        return editor;
    }

    /**
     * 保存配置
     */
    public static boolean saveString(Context context, String key, String value) {
        return getEditor(context).putString(key, value).commit();
    }

    /**
     * 保存配置
     */
    public static boolean saveInt(Context context, String key, int value) {
        return getEditor(context).putInt(key, value).commit();
    }

    /**
     * 保存配置
     */
    public static boolean saveBoolean(Context context, String key, boolean value) {
        return getEditor(context).putBoolean(key, value).commit();
    }

    /**
     * 保存配置
     */
    public static boolean saveLong(Context context, String key, long value) {
        return getEditor(context).putLong(key, value).commit();
    }

    /**
     * 保存配置
     */
    public static boolean saveFloat(Context context, String key, float value) {
        return getEditor(context).putFloat(key, value).commit();
    }

    /**
     * 读取配置
     */
    public static String getString(Context context, String key) {
        return getPreferences(context).getString(key, null);
    }

    /**
     * 读取配置
     */
    public static int getInt(Context context, String key) {
        return getPreferences(context).getInt(key, 0);
    }

    /**
     * 读取配置
     */
    public static boolean getBoolean(Context context, String key) {
        return getPreferences(context).getBoolean(key, false);
    }

    /**
     * 读取配置
     */
    public static long getLong(Context context, String key) {
        return getPreferences(context).getLong(key, 0L);
    }

    /**
     * 读取配置
     */
    public static float getFloat(Context context, String key) {
        return getPreferences(context).getFloat(key, 0.0F);
    }
}
