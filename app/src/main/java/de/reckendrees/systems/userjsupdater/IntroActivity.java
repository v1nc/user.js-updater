package de.reckendrees.systems.userjsupdater;

import android.Manifest;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

/**
 * Created by vinc on 02.05.19.
 */

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SliderPage sliderPage = new SliderPage();
        sliderPage.setTitle(getResources().getString(R.string.intro1_title));
        sliderPage.setDescription(getResources().getString(R.string.intro1_text));
        sliderPage.setImageDrawable(R.mipmap.ic_launcher);
        sliderPage.setBgColor(Color.parseColor("#26494b"));
        addSlide(AppIntroFragment.newInstance(sliderPage));
        SliderPage sliderPage2 = new SliderPage();
        sliderPage2.setTitle(getResources().getString(R.string.intro2_title));
        sliderPage2.setDescription(getResources().getString(R.string.intro2_text));
        sliderPage2.setImageDrawable(R.drawable.ic_folder_black_24dp);
        sliderPage2.setBgColor(Color.parseColor("#26494b"));
        addSlide(AppIntroFragment.newInstance(sliderPage2));
        askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2); // OR
        // OPTIONAL METHODS
        // Override bar/separator color.
        setBarColor(Color.parseColor("#d65a4e"));
        //setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button.
        showSkipButton(false);
        //setProgressButtonEnabled(false);

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor e = getPrefs.edit();
        e.putBoolean("firstStart", false);
        e.putString("package_name","org.mozilla.fennec_fdroid");
        e.apply();
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}