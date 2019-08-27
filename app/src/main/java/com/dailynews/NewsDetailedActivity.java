package com.dailynews;

import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NewsDetailedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newsdetailed);

        TextView title = findViewById(R.id.news_detailed_title);
        TextView image_source = findViewById(R.id.news_detailed_image_source);
        ImageView imageView = findViewById(R.id.news_detailed_image);
        WebView webView = findViewById(R.id.webView);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.new_toolbar);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        String stitle = getIntent().getStringExtra("title");
        String image = getIntent().getStringExtra("image");
        String simage_source = getIntent().getStringExtra("image_source");
        String data = getIntent().getStringExtra("body");

        title.setText(stitle);
        Glide.with(this).load(image).into(imageView);
        image_source.setText(simage_source);
        webView.getSettings().setDefaultTextEncodingName("UTF-8") ;//防止低版本显示新闻为乱码
        webView.loadData(getNewContent(data),"text/html; charset=UTF-8",null);
    }

    //解决新闻WebView内容图片过大
    private String getNewContent(String url) {
        Document doc = Jsoup.parse(url);//使用Jsoup将Url转成Document格式，以便于抓取网页数据
        Elements elements = doc.getElementsByClass("content-image");//根据className获取图片，头像的className为"avatar"，新闻内容图片的classNmae为"content-image"
        //对图片长宽进行处理
        for (Element element : elements) {
            element.attr("width", "100%").attr("height", "auto");
        }
        return doc.toString();//转回Url格式
    }

    //设置Menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news_toolbar, menu);
        return true;
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.share:
                Toast.makeText(NewsDetailedActivity.this,"You clicked share",Toast.LENGTH_SHORT).show();
                break;
            case R.id.collect:
                Toast.makeText(NewsDetailedActivity.this,"You clicked collect",Toast.LENGTH_SHORT).show();
                break;
            case R.id.comment:
                Toast.makeText(NewsDetailedActivity.this,"You clicked comment",Toast.LENGTH_SHORT).show();
                break;
            case R.id.like:
                Toast.makeText(NewsDetailedActivity.this,"You clicked praise",Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return true;
    }

}
