package com.kunfei.bookshelf.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.utils.ReadAssets;


/**
 * show version and info
 * Created by GreenSkinMonster on 2015-05-23.
 */
public class AboutFragment  extends PreferenceFragment {

    public static final String TAG_KEY = "ABOUT_KEY";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        final TextView tabContent = (TextView) view.findViewById(R.id.tab_content);
        final ScrollView scrollView = (ScrollView) view.findViewById(R.id.scroll_view);

        TextView tvAppVersion = (TextView) view.findViewById(R.id.app_version);
        tvAppVersion.setText(
                getResources().getString(R.string.app_name) + " " + MApplication.getVersionName()
                        + "");

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        TabLayout.Tab notesTab = tabLayout.newTab().setText("更新记录");
        //TabLayout.Tab donarTab = tabLayout.newTab().setText("捐助名单");
        TabLayout.Tab linksTab = tabLayout.newTab().setText("感谢");
        TabLayout.Tab disclaimerTab = tabLayout.newTab().setText("免责声明");

        tabLayout.addTab(notesTab);
        //tabLayout.addTab(donarTab);
        tabLayout.addTab(linksTab);
        tabLayout.addTab(disclaimerTab);

        tabContent.setText(getContent(0));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                scrollView.scrollTo(0, 0);
                tabContent.setText(getContent(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                scrollView.scrollTo(0, 0);
            }
        });

        //setActionBarTitle("关于");

        setActionBarTitle(R.string.about);

        return view;
    }

    void setActionBarTitle(@StringRes int resId) {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(resId);
        }
    }

    private String getContent(int position) {
        String file = "updateLog.md";
        if (position == 1) {
            //file = "donors.txt";
            file = "license.txt";
        } else if (position == 2) {

            file = "disclaimer.md";
        }else if (position == 3) {

        }

        try {
            return ReadAssets.getText(getActivity(), file);
        } catch (Exception e) {
            return e.getMessage();
        }

    }


}
