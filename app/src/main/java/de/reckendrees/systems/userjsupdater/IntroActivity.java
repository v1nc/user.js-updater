package de.reckendrees.systems.userjsupdater;

import android.Manifest;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntro2;
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
        sliderPage.setImageDrawable(R.mipmap.ic_launcher_foreground);
        sliderPage.setBgColor(Color.parseColor("#1b1b1b"));
        addSlide(AppIntroFragment.newInstance(sliderPage));
        SliderPage sliderPage2 = new SliderPage();
        sliderPage2.setTitle(getResources().getString(R.string.intro2_title));
        sliderPage2.setDescription(getResources().getString(R.string.intro2_text));
        sliderPage2.setImageDrawable(R.drawable.ic_folder_black_24dp);
        sliderPage2.setBgColor(Color.parseColor("#1b1b1b"));
        addSlide(AppIntroFragment.newInstance(sliderPage2));
        askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2); // OR
        // OPTIONAL METHODS
        // Override bar/separator color.
        setBarColor(Color.parseColor("#e86c2e"));
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
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}