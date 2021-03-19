/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.settings.display;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import com.android.settings.widget.SeekBarPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import com.softwinner.AWDisplay;

public class AwEnhanceModePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY_ENHANCE_MODE_SETTING = "enhance_mode_setting";
    private static final String KEY_ENHANCE_MODE = "enhance_mode";
    private static final String KEY_ENHANCE_MODE_DEMO = "enhance_mode_demo";
    private static final String KEY_COLOR_BRIGHT_SETTING = "color_bright_setting";
    private static final String KEY_CONTRAST_SETTING = "contrast_setting";
    private static final String KEY_SATURATION_SETTING = "saturation_setting";
    private Context mContext;
    private SwitchPreference mEnhanceMode;
    private SwitchPreference mDemo;
    private SeekBarPreference mBright;
    private SeekBarPreference mContrast;
    private SeekBarPreference mSaturation;
    private int maxRange;

    public AwEnhanceModePreferenceController(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        if (isAvailable()) {
            setVisible(screen, getPreferenceKey(), true);
            mEnhanceMode = (SwitchPreference) screen.findPreference(KEY_ENHANCE_MODE);
            mEnhanceMode.setOnPreferenceChangeListener(this);
            mDemo = (SwitchPreference) screen.findPreference(KEY_ENHANCE_MODE_DEMO);
            mDemo.setOnPreferenceChangeListener(this);
            if (isHasHSL()) {
                try {
                    maxRange = mContext.getResources().getInteger(R.integer.config_HSL_max_range);
                } catch (NotFoundException e) {
                    maxRange = 10;       // default max range is 10 for pad.
                }
                mBright = (SeekBarPreference) screen.findPreference(KEY_COLOR_BRIGHT_SETTING);
                mBright.setOnPreferenceChangeListener(this);
                mBright.setMax(maxRange);
                mContrast = (SeekBarPreference) screen.findPreference(KEY_CONTRAST_SETTING);
                mContrast.setOnPreferenceChangeListener(this);
                mContrast.setMax(maxRange);
                mSaturation = (SeekBarPreference) screen.findPreference(KEY_SATURATION_SETTING);
                mSaturation.setOnPreferenceChangeListener(this);
                mSaturation.setMax(maxRange);
            } else {
                PreferenceCategory screen_enhance = (PreferenceCategory)(screen.findPreference(KEY_ENHANCE_MODE_SETTING));
                mBright = (SeekBarPreference) screen.findPreference(KEY_COLOR_BRIGHT_SETTING);
                mContrast = (SeekBarPreference) screen.findPreference(KEY_CONTRAST_SETTING);
                mSaturation = (SeekBarPreference) screen.findPreference(KEY_SATURATION_SETTING);
                screen_enhance.removePreference(mBright);
                screen_enhance.removePreference(mContrast);
                screen_enhance.removePreference(mSaturation);
            }
        } else {
            setVisible(screen, getPreferenceKey(), false);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private boolean isHasHSL() {
        try {
            return mContext.getResources().getBoolean(R.bool.config_has_HSL);
        } catch (NotFoundException e) {
            return false;     // default do not show HSL for pad.
        }
	}

    @Override
    public String getPreferenceKey() {
        return KEY_ENHANCE_MODE_SETTING;
    }

    @Override
    public void updateState(Preference preference) {
        boolean enabled = AWDisplay.getColorEnhance();
        boolean demo = AWDisplay.getColorEnhanceDemo();
        if (mEnhanceMode != null)
            mEnhanceMode.setChecked(enabled);
        if (mDemo != null) {
            mDemo.setEnabled(enabled);
            mDemo.setChecked(demo);
        }
        if (mBright != null) {
            int bright = AWDisplay.getHSLBright();
            if (bright < 0 || bright > maxRange)
                bright = maxRange / 2;
            mBright.setProgress(bright);
            mBright.setEnabled(enabled);
        }
        if (mContrast != null) {
            int contrast = AWDisplay.getHSLContrast();
            if (contrast < 0 || contrast > maxRange)
                contrast = maxRange / 2;
            mContrast.setProgress(contrast);
            mContrast.setEnabled(enabled);
        }
        if (mSaturation != null) {
            int saturation = AWDisplay.getHSLSaturation();
            if (saturation < 0 || saturation > maxRange)
                saturation = maxRange / 2;
            mSaturation.setProgress(saturation);
            mSaturation.setEnabled(enabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mEnhanceMode == preference) {
            boolean auto = (Boolean) newValue;
            AWDisplay.setColorEnhance(auto);
            if (mDemo != null) mDemo.setEnabled(auto);
            if (mBright != null) mBright.setEnabled(auto);
            if (mContrast != null) mContrast.setEnabled(auto);
            if (mSaturation	!= null) mSaturation.setEnabled(auto);
        } else if (mDemo == preference) {
            boolean auto = (Boolean) newValue;
            AWDisplay.setColorEnhanceDemo(auto);
        } else if (mBright == preference) {
            int auto = (Integer) newValue;
            AWDisplay.setHSLBright(auto);
        } else if (mContrast == preference) {
            int auto = (Integer) newValue;
            AWDisplay.setHSLContrast(auto);
        } else if (mSaturation == preference) {
            int auto = (Integer) newValue;
            AWDisplay.setHSLSaturation(auto);
        }
        return true;
    }
}
