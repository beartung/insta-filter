package org.insta.example;

import android.app.Activity;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import android.text.Html;
import android.text.method.LinkMovementMethod;

import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;

import butterknife.InjectView;

import com.astuetz.PagerSlidingTabStrip;

import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.util.ArrayList;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

import org.insta.InstaFilter;

public class Demo extends ActionBarActivity implements ViewPager.OnPageChangeListener, Target, Handler.Callback { 
    private static final int MSG_SWITH_FILTER = 1001;

    @InjectView(R.id.image) GPUImageView mImageView;

    @InjectView(R.id.pager) ViewPager pager;

    @InjectView(R.id.tabs) PagerSlidingTabStrip tabs;

    @InjectView(R.id.link) TextView link;

    FilterPageAdapter adapter;

    HandlerThread thread;

    Handler handler;

    int mFilter;
    int mWidth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        ButterKnife.inject(this);

        WindowManager w = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display d = w.getDefaultDisplay();
        int rotation = d.getRotation();
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            mWidth = d.getWidth();
        } else {
            mWidth = d.getHeight();
        }
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(mWidth, mWidth);
        mImageView.setLayoutParams(p);

        adapter = new FilterPageAdapter(getSupportFragmentManager());
        pager.setOffscreenPageLimit(0);
        pager.setAdapter(adapter);
        tabs.setViewPager(pager);
        tabs.setOnPageChangeListener(this);

        thread = new HandlerThread("filter");
        thread.start();
        handler = new Handler(thread.getLooper(), this);

        Picasso.with(this).load(R.drawable.hhkb).into(this);

        link.setText(Html.fromHtml("<a href=\"https://github.com/beartung/insta-filter\">insta-filter@github</a>"));
        link.setMovementMethod(LinkMovementMethod.getInstance());

    }

    public void onDestroy() {
        super.onDestroy();
        if (thread != null) thread.quit();
        FilterHelper.destroyFilters();
    }

    private class FilterPageAdapter extends FragmentPagerAdapter {

        private final int[] TITLES = {
            R.string.filter_normal,
            R.string.filter_amaro,
            R.string.filter_rise,
            R.string.filter_hudson,
            R.string.filter_xproii,
            R.string.filter_sierra,
            R.string.filter_lomo,
            R.string.filter_earlybird,
            R.string.filter_sutro,
            R.string.filter_toaster,
            R.string.filter_brannan,
            R.string.filter_inkwell,
            R.string.filter_walden,
            R.string.filter_hefe,
            R.string.filter_valencia,
            R.string.filter_nashville,
            R.string.filter_1977,
            R.string.filter_lordkelvin
        };

        public FilterPageAdapter(FragmentManager fm) {
            super(fm);
        }

        public CharSequence getPageTitle(int position) {
            return getString(TITLES[position]);
        }

        public int getCount() {
            return TITLES.length;
        }

        public Fragment getItem(int position) {
            return new DummyFragment();
        }
    };

    private class DummyFragment extends Fragment {

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return new TextView(Demo.this);
        }

    };

    public void onPageScrollStateChanged(int state){
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void onPageSelected(int position) {
        if (position != mFilter) {
            handler.removeMessages(MSG_SWITH_FILTER);
            Message m = handler.obtainMessage(MSG_SWITH_FILTER);
            m.arg1 = position;
            handler.sendMessage(m);
        }
    }

    public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
        mImageView.setImage(bitmap);
    }

    public void onBitmapFailed(Drawable errorDrawable) {
    }

    public void onPrepareLoad(Drawable placeHolderDrawable) {
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SWITH_FILTER:
                int index = msg.arg1;
                mFilter = index;
                try {
                    InstaFilter filter = FilterHelper.getFilter(this, mFilter);
                    if (filter != null) {
                        mImageView.setFilter(filter);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }

}
