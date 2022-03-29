package org.prebid.mobile.drprebid.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.ui.fragments.ImageFragment;
import org.prebid.mobile.drprebid.ui.views.SlideIndicatorsView;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    private ViewPager pager;
    private PagerAdapter pagerAdapter;
    private SlideIndicatorsView indicatorsView;
    private Button skipButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }

        pager = findViewById(R.id.pager_images);
        pagerAdapter = new SlideImageAdapter(getSupportFragmentManager());
        pager.addOnPageChangeListener(onPageChangeListener);
        pager.setAdapter(pagerAdapter);

        indicatorsView = findViewById(R.id.view_indicators);
        indicatorsView.setSelectedPosition(0);

        skipButton = findViewById(R.id.button_skip);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
    }

    private final ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (position == pagerAdapter.getCount() - 1) {
                skipButton.setText(R.string.action_continue);
            } else {
                skipButton.setText(R.string.action_skip);
            }

            indicatorsView.setSelectedPosition(position);
        }
    };

    private class SlideImageAdapter extends FragmentStatePagerAdapter {

        private final List<ImageFragment> fragments;

        SlideImageAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);

            fragments = new ArrayList<>();
            fragments.add(ImageFragment.newInstance(R.drawable.welcome_1));
            fragments.add(ImageFragment.newInstance(R.drawable.welcome_2));
            fragments.add(ImageFragment.newInstance(R.drawable.welcome_3));
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
