package me.blog.korn123.easyphotomap.activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.utils.CommonUtils;

/**
 * Created by CHO HANJOONG on 2016-08-15.
 */
public class EPMPreferenceActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        findPreference("enableDateFilter").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.valueOf(newValue.toString());
                CommonUtils.saveBooleanPreference(EPMPreferenceActivity.this, "enableDateFilter", checked);
                return true;
            }
        });
    }

}
