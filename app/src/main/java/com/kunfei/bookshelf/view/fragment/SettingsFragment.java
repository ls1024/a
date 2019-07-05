package com.kunfei.bookshelf.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;

import com.kunfei.bookshelf.constant.AppConstant;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.FileHelp;

import com.kunfei.bookshelf.help.UpdateManager;
import com.kunfei.bookshelf.utils.FileUtils;
import com.kunfei.bookshelf.utils.PermissionUtils;
import com.kunfei.bookshelf.utils.TimeUtils;
import com.kunfei.bookshelf.view.activity.MySettingActivity;

import java.util.Date;
import java.util.Objects;

import com.kunfei.bookshelf.widget.filepicker.picker.FilePicker;

import static android.app.Activity.RESULT_OK;

/**
 * Created by GKF on 2017/12/16.
 * 设置
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private MySettingActivity settingActivity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("CONFIG");
        addPreferencesFromResource(R.xml.pref_settings);
        settingActivity = (MySettingActivity) this.getActivity();
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        if ("".equals(sharedPreferences.getString(getString(R.string.pk_download_path), ""))) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.pk_download_path), FileHelp.getCachePath());
            editor.apply();
        }
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pk_bookshelf_px)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pk_download_path)));

        Preference abountPre = findPreference(getString(R.string.pk_about));

        abountPre.setSummary(MApplication.getVersionName());

        abountPre.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                Intent intent = new Intent(getActivity(), MySettingActivity.class);
                intent.putExtra(MySettingActivity.TAG_KEY,AboutFragment.TAG_KEY);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getActivity(), R.anim.slide_in_right, 0);
                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
                return true;
            }
        });

        final Preference checkPreference = findPreference("PERF_LAST_UPDATE_CHECK");
        checkPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                checkPreference.setSummary("上次检查 ：" + TimeUtils.date2String(new Date()));
                UpdateManager.getInstance(getActivity()).checkUpdate(true);
                return true;
            }
        });
        String lastCheckTime = MApplication.getInstance().getLastCheckTime();
        if (lastCheckTime != null) {
            checkPreference.setSummary("上次检查 ：" + lastCheckTime);
        } else {
            checkPreference.setSummary("上次检查 ：- ");
        }

    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (Preference preference, Object value)-> {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        } else if (preference instanceof MultiSelectListPreference) {
            MultiSelectListPreference listPreference = (MultiSelectListPreference) preference;
            //int index = listPreference.getValues()
            // Set the summary to reflect the new value.
            preference.setSummary(stringValue);
        } else {
            // For all other preferences, set the summary to the value's
            preference.setSummary(stringValue);
        }
        return true;
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                preference.getContext().getSharedPreferences("CONFIG", Context.MODE_PRIVATE).getString(preference.getKey(), ""));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pk_ImmersionStatusBar)) || key.equals(getString(R.string.pk_navigationBarColorChange))) {
            settingActivity.initImmersionBar();
            RxBus.get().post(RxBusTag.IMMERSION_CHANGE, true);
        } else if (key.equals(getString(R.string.pk_bookshelf_px))) {
            RxBus.get().post(RxBusTag.UPDATE_PX, true);
        }else if (key.equals("showAllFind")) {
            RxBus.get().post(RxBusTag.UPDATE_BOOK_SOURCE, true);
        }else  if (key.equals(getString(R.string.pk_bookshelf_show))) {
            RxBus.get().post(RxBusTag.UPDATE_BOOK_SOURCE, false);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

         long mLastClickTime = 0;

        if (preference.getKey().equals(getString(R.string.pk_download_path))) {
            selectDownloadPath(preference);
        }

        if (preference.getKey().equals(getString(R.string.pk_left_menu_header))) {
            //selectLogoPath(preference);




            //avoid double click
            long currentClickTime = System.currentTimeMillis();
            long elapsedTime = currentClickTime - mLastClickTime;
            mLastClickTime = currentClickTime;
            if (elapsedTime <= AppConstant.MIN_CLICK_INTERVAL)
                return true;

            Intent intent = new Intent(getActivity(), MySettingActivity.class);
            intent.putExtra(MySettingActivity.TAG_KEY, "updateUI");
            ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getActivity(), R.anim.slide_in_right, 0);
            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());





        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


    private void selectDownloadPath(Preference preference) {
        PermissionUtils.checkMorePermissions(getActivity(), MApplication.PerList, new PermissionUtils.PermissionCheckCallback() {
            @Override
            public void onHasPermission() {
                FilePicker picker = new FilePicker(getActivity(), FilePicker.DIRECTORY);
                picker.setBackgroundColor(getResources().getColor(R.color.background));
                picker.setTopBackgroundColor(getResources().getColor(R.color.background));
                picker.setRootPath(preference.getSummary().toString());
                picker.setItemHeight(30);
                picker.setOnFilePickListener(currentPath -> {
                    if (!currentPath.contains(FileUtils.getSdCardPath())) {
                        MApplication.getInstance().setDownloadPath(null);
                    } else {
                        MApplication.getInstance().setDownloadPath(currentPath);
                    }
                    preference.setSummary(MApplication.downloadPath);
                });
                picker.show();
                picker.getCancelButton().setText(R.string.restore_default);
                picker.getCancelButton().setOnClickListener(view -> {
                    picker.dismiss();
                    MApplication.getInstance().setDownloadPath(null);
                    preference.setSummary(MApplication.downloadPath);
                });
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                Toast.makeText(getActivity(), R.string.set_download_per, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAlreadyTurnedDownAndNoAsk(String... permission) {
                Toast.makeText(getActivity(), R.string.set_download_per, Toast.LENGTH_SHORT).show();
                PermissionUtils.requestMorePermissions(getActivity(), MApplication.PerList, MApplication.RESULT__PERMS);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }
}
