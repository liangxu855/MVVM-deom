package com.example.administrator.mvvm_deom.viewModel;

import android.app.Activity;
import android.content.Context;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import com.example.administrator.mvvm_deom.MyApplication;
import com.example.administrator.mvvm_deom.R;
import com.example.administrator.mvvm_deom.model.TopNews;
import com.kelin.mvvmlight.base.ViewModel;
import com.kelin.mvvmlight.messenger.Messenger;

import java.util.List;

import me.tatarka.bindingcollectionadapter.ItemView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Administrator on 2018-02-28.
 */

public class MainActivityViewModel implements ViewModel {

    public static final String TOKEN_UPDATE_INDICATOR = "token_update_indicator" + MyApplication.sPackageName;

    private Context context;

    public final ItemView topItemView = ItemView.of(com.example.administrator.mvvm_deom.BR.viewModel, R.layout.viewpager_item_top_news);
    public final ObservableList<TopItemViewModel> topItemViewModel = new ObservableArrayList<>();

    public MainActivityViewModel(final Activity activity) {
        context = activity;
        Messenger.getDefault().register(activity, NewsViewModel.TOKEN_TOP_NEWS_FINISH, TopNews.class, new Action1<TopNews>() {
            @Override
            public void call(TopNews topNews) {
                Observable.just(topNews)
                        .doOnNext(new Action1<TopNews>() {
                            @Override
                            public void call(TopNews topNews) {
                                topItemViewModel.clear();
                            }
                        })
                        .flatMap(new Func1<TopNews, Observable<TopNews.TopStoriesBean>>() {
                            @Override
                            public Observable<TopNews.TopStoriesBean> call(TopNews topNews) {
                                return Observable.from(topNews.getTop_stories());
                            }
                        })
                        .doOnNext(new Action1<TopNews.TopStoriesBean>() {
                            @Override
                            public void call(TopNews.TopStoriesBean topStoriesBean) {
                                topItemViewModel.add(new TopItemViewModel(context,topStoriesBean));
                            }
                        })
                        .toList()
                        .subscribe(new Action1<List<TopNews.TopStoriesBean>>() {
                            @Override
                            public void call(List<TopNews.TopStoriesBean> topStoriesBeen) {
                                Messenger.getDefault().sendNoMsgToTargetWithToken(TOKEN_UPDATE_INDICATOR, activity);
                            }
                        });
            }
        });
    }
}
