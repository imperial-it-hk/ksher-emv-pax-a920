package com.evp.payment.ksher.utils.sharedpreferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.preference.PreferenceManager;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.google.gson.reflect.TypeToken;

import java.util.Set;

import static android.os.Build.VERSION_CODES.HONEYCOMB;


public class SharedPreferencesUtil {
    private static Context context;
    private static RxSharedPreferences rxPreferences;

    public static void init(Context appContext) {
        context = appContext;

        if (rxPreferences == null) {
            android.content.SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            rxPreferences = RxSharedPreferences.create(preferences);
        }

    }

    /**
     * Create a boolean preference for {@code key}. Default is {@code false}.
     */
    @CheckResult
    @NonNull
    public static Preference<Boolean> getBoolean(@NonNull String key) {
        return rxPreferences.getBoolean(key);
    }

    /**
     * Create a boolean preference for {@code key} with a default of {@code defaultValue}.
     */
    @CheckResult
    @NonNull
    public static Preference<Boolean> getBoolean(@NonNull String key, @Nullable Boolean defaultValue) {
        return rxPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Create an enum preference for {@code key}. Default is {@code null}.
     */
    @CheckResult
    @NonNull
    public static <T extends Enum<T>> Preference<T> getEnum(@NonNull String key,
                                                            @NonNull Class<T> enumClass) {
        return rxPreferences.getEnum(key, null, enumClass);
    }

    /**
     * Create an enum preference for {@code key} with a default of {@code defaultValue}.
     */
    @CheckResult
    @NonNull
    public static <T extends Enum<T>> Preference<T> getEnum(@NonNull String key, @Nullable T defaultValue,
                                                            @NonNull Class<T> enumClass) {
        return rxPreferences.getEnum(key, defaultValue, enumClass);
    }

    /**
     * Create a float preference for {@code key}. Default is {@code 0}.
     */
    @CheckResult
    @NonNull
    public static Preference<Float> getFloat(@NonNull String key) {
        return rxPreferences.getFloat(key);
    }

    /**
     * Create a float preference for {@code key} with a default of {@code defaultValue}.
     */
    @CheckResult
    @NonNull
    public static Preference<Float> getFloat(@NonNull String key, @Nullable Float defaultValue) {
        return rxPreferences.getFloat(key, defaultValue);
    }

    /**
     * Create an integer preference for {@code key}. Default is {@code 0}.
     */
    @CheckResult
    @NonNull
    public static Preference<Integer> getInteger(@NonNull String key) {
        return rxPreferences.getInteger(key);
    }

    /**
     * Create an integer preference for {@code key} with a default of {@code defaultValue}.
     */
    @CheckResult
    @NonNull
    public static Preference<Integer> getInteger(@NonNull String key, @Nullable Integer defaultValue) {
        return rxPreferences.getInteger(key, defaultValue);
    }

    /**
     * Create a long preference for {@code key}. Default is {@code 0}.
     */
    @CheckResult
    @NonNull
    public static Preference<Long> getLong(@NonNull String key) {
        return rxPreferences.getLong(key);
    }

    /**
     * Create a long preference for {@code key} with a default of {@code defaultValue}.
     */
    @CheckResult
    @NonNull
    public static Preference<Long> getLong(@NonNull String key, @Nullable Long defaultValue) {
        return rxPreferences.getLong(key, defaultValue);
    }

    /**
     * Create a preference of type {@code T} for {@code key}. Default is {@code null}.
     */
    @CheckResult
    @NonNull
    public static <T> Preference<T> getObject(@NonNull String key, @NonNull Preference.Adapter<T> adapter) {
        return rxPreferences.getObject(key, adapter);
    }

    /**
     * Create a preference of type {@code T} for {@code key}. Default is {@code null}.
     */
    @CheckResult
    @NonNull
    public static <T> Preference<T> getObject(@NonNull String key, @NonNull Class clazz) {
        return rxPreferences.getObject(key, new GsonPreferenceAdapter(clazz));
    }

    /**
     * Create a preference of type {@code T} for {@code key}. Default is {@code null}.
     */
    @CheckResult
    @NonNull
    public static <T> Preference<T> getObject(@NonNull String key, @NonNull TypeToken<T> typeReference) {
        return rxPreferences.getObject(key, new GsonPreferenceAdapter(typeReference));
    }

    /**
     * Create a preference of type {@code T} for {@code clazz} with a default of {@code defaultValue}.
     * <p>
     * The key is generated automatically with the value {@code clazz.getName()}.
     */
    @CheckResult
    @NonNull
    public static <T> Preference<T> getObject(Class<T> clazz, @Nullable T defaultValue) {
        return getObject(clazz.getName(), defaultValue, new GsonPreferenceAdapter(clazz));
    }


    /**
     * Create a preference for type {@code T} for {@code key} with a default of {@code defaultValue}.
     */
    @CheckResult
    @NonNull
    public static <T> Preference<T> getObject(@NonNull String key, @Nullable T defaultValue,
                                              @NonNull Preference.Adapter<T> adapter) {
        return rxPreferences.getObject(key, defaultValue, adapter);
    }

    /**
     * Create a preference for type {@code T} for {@code key} with a default of {@code defaultValue}.
     */
    @CheckResult
    @NonNull
    public static <T> Preference<T> getObject(@NonNull String key, @Nullable T defaultValue,
                                              @NonNull Class clazz) {
        return rxPreferences.getObject(key, defaultValue, new GsonPreferenceAdapter<>(clazz));
    }

    /**
     * Create a preference for type {@code T} for {@code key} with a default of {@code defaultValue}.
     */
    @CheckResult
    @NonNull
    public static <T> Preference<T> getObject(@NonNull String key, @Nullable T defaultValue,
                                              @NonNull TypeToken<T> typeReference) {
        return rxPreferences.getObject(key, defaultValue, new GsonPreferenceAdapter<>(typeReference));
    }

    /**
     * Create a string preference for {@code key}. Default is {@code null}.
     */
    @CheckResult
    @NonNull
    public static Preference<String> getString(@NonNull String key) {
        return rxPreferences.getString(key);
    }

    /**
     * Create a string preference for {@code key} with a default of {@code defaultValue}.
     */
    @CheckResult
    @NonNull
    public static Preference<String> getString(@NonNull String key, @Nullable String defaultValue) {
        return rxPreferences.getString(key, defaultValue);
    }

    /**
     * Create a string set preference for {@code key}. Default is an empty set.
     */
    @TargetApi(HONEYCOMB)
    @CheckResult
    @NonNull
    public static Preference<Set<String>> getStringSet(@NonNull String key) {
        return rxPreferences.getStringSet(key);
    }

    /**
     * Create a string set preference for {@code key} with a default of {@code defaultValue}.
     */
    @TargetApi(HONEYCOMB)
    @CheckResult
    @NonNull
    public static Preference<Set<String>> getStringSet(@NonNull String key,
                                                       @NonNull Set<String> defaultValue) {
        return rxPreferences.getStringSet(key, defaultValue);
    }
}
