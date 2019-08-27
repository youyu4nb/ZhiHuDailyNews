package com.dailynews;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import com.dailynews.Decoration.SpacesItemDecoration;
import com.dailynews.adapter.RecyclerViewAdapter;
import com.dailynews.adapter.ViewPagerAdapter;
import com.dailynews.gsonModel.BriefStory;
import com.dailynews.gsonModel.BriefTopStory;
import com.dailynews.gsonModel.NewsList;
import com.dailynews.util.HttpsUtil;
import com.dailynews.util.JsonUtil;
import com.google.android.material.navigation.NavigationView;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import me.relex.circleindicator.CircleIndicator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

//news为在sharedPreferences中的缓存
public class MainActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private RecyclerViewAdapter recyclerViewAdapter;
    private RecyclerViewAdapter recyclerViewAdapter2;
    private RecyclerViewAdapter recyclerViewAdapter3;
    private RecyclerView recyclerView;
    private RecyclerView recyclerView2;
    private RecyclerView recyclerView3;
    private CircleIndicator circleIndicator;
    private final String newsListAddress="https://news-at.zhihu.com/api/4/news/latest";
    private final String newsAddress="https://news-at.zhihu.com/api/4/news/before/";
    private List<BriefStory> briefStories = new ArrayList<>();
    private List<BriefStory> briefStories2 = new ArrayList<>();
    private List<BriefStory> briefStories3 = new ArrayList<>();
    private List<BriefTopStory> briefTopStories = new ArrayList<>();
    private DrawerLayout mDrawerLayout;
    private Date date;
    private Date yesterday;
    private Date before_yesterday;
    private SimpleDateFormat simpleDateFormat;
    private SimpleDateFormat simpleDateFormat2;
    private TextView yesterday_date;
    private TextView before_yesterday_date;
    private TextView collects;
    private TextView offline_download;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //如果有actionBar,显示导航按钮
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        //滑动菜单
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setCheckedItem(R.id.nav_home);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                mDrawerLayout.closeDrawers();//关闭滑动菜单
                return true;
            }
        });

//        collects = findViewById(R.id.collects);
//        offline_download = findViewById(R.id.offline_download);
//        collects.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(MainActivity.this,"You clicked collects",Toast.LENGTH_SHORT).show();
//            }
//        });
//        offline_download.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(MainActivity.this,"You clicked offline_download",Toast.LENGTH_SHORT).show();
//            }
//        });

        initViewPager();//初始化ViewPager

        //为viewPager设置小圆点
        circleIndicator = findViewById(R.id.circleindicator);
        circleIndicator.setViewPager(viewPager);

        //获取当天系统时间
        simpleDateFormat = new SimpleDateFormat("yyyyMMdd");//设置时间格式
        simpleDateFormat2 = new SimpleDateFormat("yyyy年MM月dd日");//设置时间格式
        date = new Date(System.currentTimeMillis());

        //获取昨天时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(calendar.DATE,-1);
        yesterday = calendar.getTime();

        //获取前天时间
        calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(calendar.DATE,-2);
        before_yesterday = calendar.getTime();

        //设置recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        LinearLayoutManager layoutManager3 = new LinearLayoutManager(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView2 = findViewById(R.id.recyclerView2);
        recyclerView3 = findViewById(R.id.recyclerView3);
        yesterday_date = findViewById(R.id.date2);
        before_yesterday_date = findViewById(R.id.date3);
        yesterday_date.setText(simpleDateFormat2.format(yesterday));
        before_yesterday_date.setText(simpleDateFormat2.format(before_yesterday));
        recyclerViewAdapter = new RecyclerViewAdapter(briefStories);
        recyclerViewAdapter2 = new RecyclerViewAdapter(briefStories2);
        recyclerViewAdapter3 = new RecyclerViewAdapter(briefStories3);

        //RecyclerView设置item间距
        SpacesItemDecoration spacesItemDecoration = new SpacesItemDecoration(30);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(spacesItemDecoration);
        recyclerView2.setHasFixedSize(true);
        recyclerView2.setNestedScrollingEnabled(false);
        recyclerView2.setAdapter(recyclerViewAdapter2);
        recyclerView2.setLayoutManager(layoutManager2);
        recyclerView2.addItemDecoration(spacesItemDecoration);
        recyclerView3.setHasFixedSize(true);
        recyclerView3.setNestedScrollingEnabled(false);
        recyclerView3.setAdapter(recyclerViewAdapter3);
        recyclerView3.setLayoutManager(layoutManager3);
        recyclerView3.addItemDecoration(spacesItemDecoration);

        //设置下拉刷新
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestByServer();
            }
        });

        requestNewsList();//请求新闻列表

        //将状态栏和背景融为一体
//        if(Build.VERSION.SDK_INT >= 21){
//            View decorView = getWindow().getDecorView();
//            //将活动得布局显示在状态栏上面
//            decorView.setSystemUiVisibility
//                    (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            //将状态栏设置成透明的
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }

    }

    //设置Menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.user:
                Toast.makeText(MainActivity.this,"You clicked user",Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Toast.makeText(MainActivity.this,"You clicked settings",Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home://导航按钮的id为android.R.id.home
                mDrawerLayout.openDrawer(GravityCompat.START);//显示滑动菜单
                break;
            default:
        }
        return true;
    }

    //初始化ViewPager
    private void initViewPager() {
        //设置viewPager
        viewPagerAdapter = new ViewPagerAdapter(this,briefTopStories);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(viewPagerAdapter);
        mHandler.sendEmptyMessageDelayed(0, 1000*5);
    }

    //ViewPager自动播放
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int count = viewPagerAdapter.getCount();
            int index = viewPager.getCurrentItem();
            index = (index+1)%count;
            viewPager.setCurrentItem(index);    //收到消息后设置viewPager当前要显示的图片
            mHandler.sendEmptyMessageDelayed(0, 1000*5);    //第一个参数随便写；第二个参数表示每两秒刷新一次
        }
    };

    //请求并解析新闻列表(优先使用缓存中的内容)
    private void requestNewsList(){
        //判断缓存
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String data = sharedPreferences.getString("newslist",null);
        String data2 = sharedPreferences.getString("news2",null);
        String data3 = sharedPreferences.getString("news3",null);
        if(data != null && data2 != null && data3 != null){
            setNewsList(data);
            setNews2(data2);
            setNews3(data3);
        }else{
            requestByServer();
        }

    }

    //直接通过发送请求获取新闻内容
    private void requestByServer(){

        //今天数据
        HttpsUtil.sendOkHttpRequest(newsListAddress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"请求新闻列表失败",Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String data = response.body().string();
                //将data置入缓存
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("newslist",data);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setNewsList(data);
                        swipeRefreshLayout.setRefreshing(false);

                    }
                });
            }
        });

        //昨天数据
        String news2Address = newsAddress + simpleDateFormat.format(date);
        HttpsUtil.sendOkHttpRequest(news2Address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"请求新闻列表失败",Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String data = response.body().string();
                //将data置入缓存
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("news2",data);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setNews2(data);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        //前天数据
        String news3Address = newsAddress + simpleDateFormat.format(yesterday);
        HttpsUtil.sendOkHttpRequest(news3Address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"请求新闻列表失败",Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String data = response.body().string();
                //将data置入缓存
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("news3",data);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setNews3(data);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    //设置新闻列表
    private void setNewsList(String data){
        NewsList newsList = JsonUtil.handleNewsListResponse(data);
        setStoriesList(newsList.getBriefStories());
        setTopStoriesList(newsList.getBriefTopStories());
    }

    //设置新闻列表2
    private void setNews2(String data){
        NewsList newsList = JsonUtil.handleNewsListResponse(data);
        setStoriesList(newsList.getBriefStories());
    }

    //设置新闻列表3
    private void setNews3(String data){
        NewsList newsList = JsonUtil.handleNewsListResponse(data);
        setStoriesList(newsList.getBriefStories());
    }

    //设置stories列表
    private void setStoriesList(List<BriefStory> briefStoryList){
        if(briefStoryList!=null&&briefStoryList.size()>0){
            switch (Thread.currentThread().getStackTrace()[3].getMethodName()){//根据方法名执行逻辑
                case "setNewsList":
                    this.briefStories.clear();
                    this.briefStories.addAll(briefStoryList);
                    recyclerViewAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(0);
                    break;
                case "setNews2":
                    this.briefStories2.clear();
                    this.briefStories2.addAll(briefStoryList);
                    recyclerViewAdapter2.notifyDataSetChanged();
                    recyclerView2.scrollToPosition(0);
                    break;
                case "setNews3":
                    this.briefStories3.clear();
                    this.briefStories3.addAll(briefStoryList);
                    recyclerViewAdapter3.notifyDataSetChanged();
                    recyclerView3.scrollToPosition(0);
                    break;
                default:
                    break;
            }
        }
    }

    //设置topStories列表
    private void setTopStoriesList(List<BriefTopStory> briefTopStoryList){
        if(briefTopStoryList!=null&&briefTopStoryList.size()>0){
            this.briefTopStories.clear();
            this.briefTopStories.addAll(briefTopStoryList);
            viewPagerAdapter.notifyDataSetChanged();
            circleIndicator.removeAllViews();
            circleIndicator.setViewPager(viewPager);
        }
    }

}
