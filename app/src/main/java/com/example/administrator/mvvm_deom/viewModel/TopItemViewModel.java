package com.example.administrator.mvvm_deom.viewModel;

import android.content.Context;
import android.databinding.ObservableField;
import android.widget.Toast;

import com.example.administrator.mvvm_deom.model.TopNews;
import com.kelin.mvvmlight.base.ViewModel;
import com.kelin.mvvmlight.command.ReplyCommand;

import rx.functions.Action0;

/**
 * Created by Administrator on 2018-02-28.
 */

public class TopItemViewModel implements ViewModel {

    private Context context;

    private TopNews.TopStoriesBean topStoriesBean;

    public final ObservableField<String> title = new ObservableField<>();
    public final ObservableField<String> imageUrl = new ObservableField<>();

    /**
     * 顶部图片的点击事件
     */
    public final ReplyCommand topItemClickCommand  = new ReplyCommand(new Action0() {
        @Override
        public void call() {
            Toast.makeText(context,"点击了topItem",Toast.LENGTH_SHORT).show();
        }
    });

    public TopItemViewModel(Context context,TopNews.TopStoriesBean topStoriesBean){
        this.context = context;
        this.topStoriesBean = topStoriesBean;

        title.set(topStoriesBean.getTitle());
        imageUrl.set(topStoriesBean.getImage());
    }
}
