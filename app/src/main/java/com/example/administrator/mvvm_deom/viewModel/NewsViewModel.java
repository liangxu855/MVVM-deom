package com.example.administrator.mvvm_deom.viewModel;

import android.app.Fragment;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableList;
import android.support.v4.util.Pair;
import android.widget.Toast;

import com.example.administrator.mvvm_deom.MyApplication;
import com.example.administrator.mvvm_deom.R;
import com.example.administrator.mvvm_deom.model.News;
import com.example.administrator.mvvm_deom.model.TopNews;
import com.example.administrator.mvvm_deom.retrofit.HttpService;
import com.example.administrator.mvvm_deom.retrofit.RetrofitProvider;
import com.example.administrator.mvvm_deom.utils.NewsListHelper;
import com.kelin.mvvmlight.base.ViewModel;
import com.kelin.mvvmlight.command.ReplyCommand;
import com.kelin.mvvmlight.messenger.Messenger;

import java.util.Calendar;

import me.tatarka.bindingcollectionadapter.BaseItemViewSelector;
import me.tatarka.bindingcollectionadapter.ItemView;
import me.tatarka.bindingcollectionadapter.ItemViewSelector;
import rx.Notification;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by Administrator on 2018-02-28.
 */

public class NewsViewModel implements ViewModel {

    public static final String TOKEN_TOP_NEWS_FINISH = "token_top_news_finish" + MyApplication.sPackageName;

    private Fragment fragment;

    private News mNews;
    //recyclerView 的条目绑定
    public final ObservableList<NewItemViewModel> itemViewModel = new ObservableArrayList<>();
    public final ItemViewSelector<NewItemViewModel> itemView = new BaseItemViewSelector<NewItemViewModel>() {
        @Override
        public void select(ItemView itemView, int position, NewItemViewModel item) {
            itemView.set(com.example.administrator.mvvm_deom.BR.viewModel, item.storiesBean.getExtraField() != null ? R.layout.listitem_news_header : R.layout.listitem_news);
        }

        @Override
        public int viewTypeCount() {
            return 2;
        }
    };

    public final ViewStyle viewStyle = new ViewStyle();

    public class ViewStyle {
        public final ObservableBoolean isRefreshing = new ObservableBoolean(true);
        public final ObservableBoolean progressRefreshing = new ObservableBoolean(true);
    }

    //刷新事件
    public final ReplyCommand onRefreshCommand = new ReplyCommand(new Action0() {
        @Override
        public void call() {
            Observable.just(Calendar.getInstance())
                    .doOnNext(new Action1<Calendar>() {
                        @Override
                        public void call(Calendar calendar) {
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                        }
                    })
                    .map(new Func1<Calendar, String>() {
                        @Override
                        public String call(Calendar calendar) {
                            return NewsListHelper.DAY_FORMAT.format(calendar.getTime());
                        }
                    })
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            loadTopNews(s);
                        }
                    });
        }
    });
    /**
     * @param p count of listview items,is unused here!
     * @params,funciton when return true，the callback just can be invoked!
     */
    public final ReplyCommand<Integer> onLoadMoreCommand = new ReplyCommand<>(new Action1<Integer>() {
        @Override
        public void call(Integer integer) {
            loadNewsList(mNews.getDate());
        }
    });

    public NewsViewModel(final Fragment fragment) {
        this.fragment = fragment;

        BehaviorSubject<Notification<News>> subject = BehaviorSubject.create();
        subject.filter(new Func1<Notification<News>, Boolean>() {
            @Override
            public Boolean call(Notification<News> newsNotification) {
                return newsNotification.isOnNext();
            }
        })
                .subscribe(new Action1<Notification<News>>() {
                    @Override
                    public void call(Notification<News> newsNotification) {
                        Toast.makeText(fragment.getActivity(), "load finish!", Toast.LENGTH_SHORT).show();
                    }
                });

        Observable.just(Calendar.getInstance())
                .doOnNext(new Action1<Calendar>() {
                    @Override
                    public void call(Calendar calendar) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                })
                .map(new Func1<Calendar, String>() {
                    @Override
                    public String call(Calendar calendar) {
                        return NewsListHelper.DAY_FORMAT.format(calendar.getTime());
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        loadTopNews(s);
                    }
                });
    }


    private void loadNewsList(String date) {
        viewStyle.isRefreshing.set(true);

        Observable<Notification<News>> newsListOb = RetrofitProvider.getInstance()
                .create(HttpService.class)
                .getNewsList(date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
            //    .compose(((FragmentLifecycleProvider) fragment).bindToLifecycle())
                .materialize().share();


        newsListOb.filter(new Func1<Notification<News>, Boolean>() {
            @Override
            public Boolean call(Notification<News> newsNotification) {
                return newsNotification.isOnNext();
            }
        }).map(new Func1<Notification<News>, News>() {
            @Override
            public News call(Notification<News> newsNotification) {
                return newsNotification.getValue();
            }
        }).filter(new Func1<News, Boolean>() {
            @Override
            public Boolean call(News news) {
                return !news.getStories().isEmpty();
            }
        }).doOnNext(new Action1<News>() {
            @Override
            public void call(News news) {
                Observable.just(news.getDate())
                        .map(new Func1<String, News.StoriesBean.ExtraField>() {
                            @Override
                            public News.StoriesBean.ExtraField call(String s) {
                                return new News.StoriesBean.ExtraField(true, s);
                            }
                        })
                        .map(new Func1<News.StoriesBean.ExtraField, News.StoriesBean>() {
                            @Override
                            public News.StoriesBean call(News.StoriesBean.ExtraField extraField) {
                                return new News.StoriesBean(extraField);
                            }
                        })
                        .subscribe(new Action1<News.StoriesBean>() {
                            @Override
                            public void call(News.StoriesBean storiesBean) {
                                itemViewModel.add(new NewItemViewModel(fragment.getActivity(), storiesBean));
                            }
                        });

            }
        }).doOnNext(new Action1<News>() {
            @Override
            public void call(News news) {
                mNews = news;
            }
        }).doAfterTerminate(new Action0() {
            @Override
            public void call() {
                viewStyle.isRefreshing.set(false);
            }
        }).flatMap(new Func1<News, Observable<News.StoriesBean>>() {
            @Override
            public Observable<News.StoriesBean> call(News news) {
                return Observable.from(news.getStories());
            }
        }).subscribe(new Action1<News.StoriesBean>() {
            @Override
            public void call(News.StoriesBean storiesBean) {
                itemViewModel.add(new NewItemViewModel(fragment.getActivity(), storiesBean));
            }
        });


        NewsListHelper.dealWithResponseError(newsListOb.filter(new Func1<Notification<News>, Boolean>() {
            @Override
            public Boolean call(Notification<News> newsNotification) {
                return newsNotification.isOnError();
            }
        }).map(new Func1<Notification<News>, Throwable>() {
            @Override
            public Throwable call(Notification<News> newsNotification) {
                return newsNotification.getThrowable();
            }
        }));

    }

    private void loadTopNews(final String date) {
        viewStyle.isRefreshing.set(true);

        Observable<TopNews> topNewsOb =
                RetrofitProvider.getInstance().create(HttpService.class)
                        .getTopNewsList();
            //            .compose(((FragmentLifecycleProvider) fragment).bindToLifecycle());

        Observable<News> newsListOb =
                RetrofitProvider.getInstance().create(HttpService.class)
                        .getNewsList(date);
         //               .compose(((FragmentLifecycleProvider) fragment).bindToLifecycle());


        Observable<Notification<Pair<TopNews, News>>> combineRequestOb = Observable.combineLatest(topNewsOb, newsListOb, new Func2<TopNews, News, Pair<TopNews, News>>() {
            @Override
            public Pair<TopNews, News> call(TopNews topNews, News news) {
                return Pair.create(topNews, news);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .materialize().share();


        combineRequestOb.filter(new Func1<Notification<Pair<TopNews, News>>, Boolean>() {
            @Override
            public Boolean call(Notification<Pair<TopNews, News>> pairNotification) {
                return pairNotification.isOnNext();
            }
        }).map(new Func1<Notification<Pair<TopNews, News>>, Pair<TopNews, News>>() {
            @Override
            public Pair<TopNews, News> call(Notification<Pair<TopNews, News>> pairNotification) {
                return pairNotification.getValue();
            }
        }).map(new Func1<Pair<TopNews, News>, TopNews>() {
            @Override
            public TopNews call(Pair<TopNews, News> topNewsNewsPair) {
                return topNewsNewsPair.first;
            }
        }).filter(new Func1<TopNews, Boolean>() {
            @Override
            public Boolean call(TopNews topNews) {
                return !topNews.getTop_stories().isEmpty();
            }
        }).doOnNext(new Action1<TopNews>() {
            @Override
            public void call(TopNews topNews) {
                Observable.just(NewsListHelper.isTomorrow(date))
                        .filter(new Func1<Boolean, Boolean>() {
                            @Override
                            public Boolean call(Boolean aBoolean) {
                                return aBoolean;
                            }
                        })
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                itemViewModel.clear();
                            }
                        });
            }
        }).subscribe(new Action1<TopNews>() {
            @Override
            public void call(TopNews topNews) {
                Messenger.getDefault().send(topNews, TOKEN_TOP_NEWS_FINISH);
            }
        });

        combineRequestOb.filter(new Func1<Notification<Pair<TopNews, News>>, Boolean>() {
            @Override
            public Boolean call(Notification<Pair<TopNews, News>> pairNotification) {
                return pairNotification.isOnNext();
            }
        }).map(new Func1<Notification<Pair<TopNews, News>>, Pair<TopNews, News>>() {
            @Override
            public Pair<TopNews, News> call(Notification<Pair<TopNews, News>> pairNotification) {
                return pairNotification.getValue();
            }
        }).map(new Func1<Pair<TopNews, News>, News>() {
            @Override
            public News call(Pair<TopNews, News> topNewsNewsPair) {
                return topNewsNewsPair.second;
            }
        }).filter(new Func1<News, Boolean>() {
            @Override
            public Boolean call(News news) {
                return !news.getStories().isEmpty();
            }
        }).doOnNext(new Action1<News>() {
            @Override
            public void call(News news) {
                mNews = news;
            }
        }).flatMap(new Func1<News, Observable<News.StoriesBean>>() {
            @Override
            public Observable<News.StoriesBean> call(News news) {
                return Observable.from(news.getStories());
            }
        }).subscribe(new Action1<News.StoriesBean>() {
            @Override
            public void call(News.StoriesBean storiesBean) {
                itemViewModel.add(new NewItemViewModel(fragment.getActivity(), storiesBean));
            }
        });

        combineRequestOb.subscribe(new Action1<Notification<Pair<TopNews, News>>>() {
            @Override
            public void call(Notification<Pair<TopNews, News>> pairNotification) {
                viewStyle.isRefreshing.set(false);
                viewStyle.progressRefreshing.set(false);
            }
        });

        NewsListHelper.dealWithResponseError(combineRequestOb.filter(new Func1<Notification<Pair<TopNews, News>>, Boolean>() {
            @Override
            public Boolean call(Notification<Pair<TopNews, News>> pairNotification) {
                return pairNotification.isOnError();
            }
        }).map(new Func1<Notification<Pair<TopNews, News>>, Throwable>() {
            @Override
            public Throwable call(Notification<Pair<TopNews, News>> pairNotification) {
                return pairNotification.getThrowable();
            }
        }));
    }
}
