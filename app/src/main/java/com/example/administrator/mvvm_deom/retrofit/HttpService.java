package com.example.administrator.mvvm_deom.retrofit;

import com.example.administrator.mvvm_deom.model.News;
import com.example.administrator.mvvm_deom.model.TopNews;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Administrator on 2018-02-28.
 */

public interface HttpService {

    //topNews
    @GET("/api/4/news/latest")
    Observable<TopNews> getTopNewsList();

    //news
    @GET("/api/4/news/before/{date}")
    Observable<News> getNewsList(@Path("date") String date);
}
