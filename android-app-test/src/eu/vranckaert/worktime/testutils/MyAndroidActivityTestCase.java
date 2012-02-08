/*
 *  Copyright 2012 Dirk Vranckaert
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.vranckaert.worktime.testutils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.utils.preferences.Preferences;

import java.util.List;
import java.util.Map;

/**
 * User: DIRK VRANCKAERT
 * Date: 24/01/12
 * Time: 8:42
 */
public abstract class MyAndroidActivityTestCase <V extends Activity> extends ActivityInstrumentationTestCase2<V> {
    /**
     * The activity that is currently in test.
     */
    public Activity activity;

    private boolean touchMode;

    /**
     * Creates a new test case for the provided activity class. TouchMode will always be set to false in this
     * constructor.
     * @param clazz The class.
     */
    public MyAndroidActivityTestCase(Class clazz) {
        this(clazz, false);
    }

    /**
     * Creates a new test case for the provided activity class. This constructor allows you to specify the touch mode
     * of the device/emulator on which the tests will be run.
     * @param clazz The class.
     * @param touchMode The touchmode ({@link Boolean#TRUE} or {@link Boolean#FALSE}).
     */
    public MyAndroidActivityTestCase(Class clazz, boolean touchMode) {
        super(clazz.getPackage().toString(), clazz);
        this.touchMode = touchMode;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(this.touchMode);

        this.activity = getActivity();

        managePreference();
    }

    /**
     * Find a view of a certain id in the activity.
     * @param id The id to look for.
     * @return The view or null if the view is not found!
     */
    public View findViewById(int id) {
        return activity.findViewById(id);
    }

    private void managePreference() {
        removePreferences(getPreferenceKeysForRemoval());
        initializePreferences(getPreferenceKeyValuePairs());
    }

    private void removePreferences(List<String> preferenceKeys) {
        for (String key : preferenceKeys) {
            Preferences.removePreference(activity, key);
        }
    }

    private void initializePreferences(Map<String, Object> preferenceKeyValuePairs) {
        if (preferenceKeyValuePairs == null || preferenceKeyValuePairs.size() == 0) {
            return;
        }

        SharedPreferences prefs = activity.getSharedPreferences(Constants.Preferences.PREFERENCES_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        for (Map.Entry<String, Object> entry : preferenceKeyValuePairs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            setPreference(key, value, editor);
        }

        editor.commit();
    }

    /**
     * Set a preference, defined by the key parameter, to a certain value.
     * @param key The key referring to the preference.
     * @param value The value of the preference to set.
     */
    public void setPreference(String key, Object value) {
        SharedPreferences prefs = activity.getSharedPreferences(Constants.Preferences.PREFERENCES_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        setPreference(key, value, editor);
        editor.commit();
    }

    private void setPreference(String key, Object value, SharedPreferences.Editor editor) {
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else {
            throw new RuntimeException("The value for key " + key + " is of an unsupported type: " + value.getClass());
        }
    }

    /**
     * A list of keys in the preferences that should be removed before launching the test class.
     * @return The list of {@link String} objects, representing a key in the preferences that should be removed.
     */
    public abstract List<String> getPreferenceKeysForRemoval();

    /**
     * A map of strings and objects. The strings represent the preferences in the system. The object is the value of the
     * preference. The entire list of preferences will be set after removing the preferences from the
     * {@link eu.vranckaert.worktime.testutils.MyAndroidTestCase#getPreferenceKeysForRemoval()} list. The preferences
     * are stored before launching the test class.
     * @return The map of keys and values for which preferences pairs should be created.
     */
    public abstract Map<String, Object> getPreferenceKeyValuePairs();

    /**
     * Finishes an activity and then starts it again.
     */
    public void restartActivity() {
        this.activity.finish();
        this.activity = getActivity();
    }
}
