package com.kunfei.bookshelf.view.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;


import android.widget.Toast;


import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.FileHelp;

import com.kunfei.bookshelf.utils.FileUtils;
import com.kunfei.bookshelf.utils.PermissionUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.view.activity.MySettingActivity;
import com.kunfei.bookshelf.view.activity.SettingActivity;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.kunfei.bookshelf.widget.filepicker.picker.FilePicker;
import static android.app.Activity.RESULT_OK;

/**
 * nested setting fragment
 * Created by GreenSkinMonster on 2015-09-11.
 */
public class SettingNestedFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {


    private static final int REQUEST_CODE_ALERT_RINGTONE = 1;

    public static final String TAG_KEY = "updateUI";


    private final int ResultSelectBg = 103;

    private MySettingActivity settingActivity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName("CONFIG");

        checkPreferenceResource();
    }

    @Override
    public void onStop() {
        super.onStop();

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


    protected void setActionBarTitle(CharSequence title) {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            String t = StringUtils.nullToText(title);
            if (actionBar != null && !t.equals(actionBar.getTitle())) {
                actionBar.setTitle(t);
            }
        }
    }

    void setActionBarTitle(@StringRes int resId) {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(resId);
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

      if (key.equals(getString(R.string.pk_logo_title)) || key.equals(getString(R.string.pk_logo_title_align)) ) {
            RxBus.get().post(RxBusTag.UPDATE_UI, true);
        }
    }

    private void checkPreferenceResource() {
        String key = getArguments().getString(MySettingActivity.TAG_KEY);
        // Load the preferences from an XML resource
        switch (key) {
            case "updateUI":
                setActionBarTitle(R.string.pref_ui);
                addPreferencesFromResource(R.xml.pref_ui);

                settingActivity = (MySettingActivity) this.getActivity();

                bindPreferenceSummaryToValue(findPreference(getString(R.string.pk_logo_path)));
                bindPreferenceSummaryToValue(findPreference(getString(R.string.pk_logo_title)));
                bindPreferenceSummaryToValue(findPreference(getString(R.string.pk_logo_title_align)));
                break;

            case "update":


                break;


            default:
                break;
        }
    }


    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference.getKey().equals(getString(R.string.pk_logo_path))) {
            selectLogoPath(preference);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }




    private void selectLogoPath(Preference preference) {
        PermissionUtils.checkMorePermissions(getActivity(), MApplication.PerList, new PermissionUtils.PermissionCheckCallback() {
            @Override
            public void onHasPermission() {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, ResultSelectBg);
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                Toast.makeText(getActivity(), "选择logo图片需存储权限", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAlreadyTurnedDownAndNoAsk(String... permission) {
                Toast.makeText(getActivity(), "选择logo图片需存储权限", Toast.LENGTH_SHORT).show();
                PermissionUtils.requestMorePermissions(getActivity(), MApplication.PerList, MApplication.RESULT__PERMS);
            }
        });
    }

        @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ResultSelectBg:
                if (resultCode == RESULT_OK && null != data) {
                    //Toast.makeText(getActivity(), data.getDataString(), Toast.LENGTH_SHORT).show();
                    String logoPath = FileUtils.getPath(getActivity(), data.getData());


                    MApplication.getInstance().setLogoPath(logoPath);

                    // SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
                    //SharedPreferences.Editor editor = sharedPreferences.edit();
                    // editor.putString(getString(R.string.pk_logo_path), logoPath);
                    // editor.apply();


                    final Preference logoPathPreference = findPreference(getString(R.string.pk_logo_path));
                    logoPathPreference.setSummary(logoPath);

                    RxBus.get().post(RxBusTag.UPDATE_UI, true);


                }
                break;
        }
    }


    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (Preference preference, Object value)-> {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
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



}
