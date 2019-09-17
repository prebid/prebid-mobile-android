package org.prebid.mobile.drprebid.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.ui.fragments.ImageFragment;
import org.prebid.mobile.drprebid.ui.views.SlideIndicatorsView;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private SlideIndicatorsView mIndicatorsView;
    private Button mSkipButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }

        mPager = findViewById(R.id.pager_images);
        mPagerAdapter = new SlideImageAdapter(getSupportFragmentManager());
        mPager.addOnPageChangeListener(onPageChangeListener);
        mPager.setAdapter(mPagerAdapter);

        mIndicatorsView = findViewById(R.id.view_indicators);
        mIndicatorsView.setSelectedPosition(0);

        mSkipButton = findViewById(R.id.button_skip);
        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private final ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (position == mPagerAdapter.getCount() - 1) {
                mSkipButton.setText(R.string.action_continue);
            } else {
                mSkipButton.setText(R.string.action_skip);
            }

            mIndicatorsView.setSelectedPosition(position);
        }
    };

    private class SlideImageAdapter extends FragmentStatePagerAdapter {
        private final List<ImageFragment> mFragments;

        SlideImageAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);

            mFragments = new ArrayList<>();
            mFragments.add(ImageFragment.newInstance(R.drawable.welcome_1));
            mFragments.add(ImageFragment.newInstance(R.drawable.welcome_2));
            mFragments.add(ImageFragment.newInstance(R.drawable.welcome_3));
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }
}
