package com.example.administrator.mvvm_deom.view.activity;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.administrator.mvvm_deom.R;
import com.example.administrator.mvvm_deom.utils.ViewUtils;
import com.example.administrator.mvvm_deom.view.fragment.NewsListFragment;
import com.example.administrator.mvvm_deom.viewModel.MainActivityViewModel;
import com.kelin.mvvmlight.messenger.Messenger;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.viewpagerindicator.CirclePageIndicator;

import rx.functions.Action0;

public class MainActivity extends RxAppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewDataBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setVariable(com.example.administrator.mvvm_deom.BR.viewModel, new MainActivityViewModel(this));

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        ((AppBarLayout) findViewById(R.id.appBarLayout)).addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int height = appBarLayout.getHeight() - getSupportActionBar().getHeight() - ViewUtils.getStatusBarHeight(MainActivity.this);
                int alpha = 255 * (0 - verticalOffset) / height;
                collapsingToolbarLayout.setExpandedTitleColor(Color.argb(0, 255, 255, 255));
                collapsingToolbarLayout.setCollapsedTitleTextColor(Color.argb(alpha, 255, 255, 255));
            }
        });

        final CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        Messenger.getDefault().register(this, MainActivityViewModel.TOKEN_UPDATE_INDICATOR, new Action0() {
            @Override
            public void call() {
                circlePageIndicator.setViewPager(viewPager);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        NewsListFragment fragment = new NewsListFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id==R.id.nav_gallery){
            NewsListFragment fragment = new NewsListFragment();
            getFragmentManager().beginTransaction().replace(R.id.content,fragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Messenger.getDefault().unregister(this);
    }
}
