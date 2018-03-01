package com.example.administrator.mvvm_deom.viewModel;

import android.content.Context;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.widget.Toast;

import com.example.administrator.mvvm_deom.model.News;
import com.example.administrator.mvvm_deom.utils.NewsListHelper;
import com.kelin.mvvmlight.base.ViewModel;
import com.kelin.mvvmlight.command.ReplyCommand;

import rx.functions.Action0;

/**
 * Created by Administrator on 2018-02-28.
 */

public class NewItemViewModel implements ViewModel{

    private Context context;

    public News.StoriesBean storiesBean;

    public final ObservableField<String> title = new ObservableField<>();
    public final ObservableField<String> imageUrl = new ObservableField<>();
    public final ObservableField<String> date = new ObservableField<>();
    public ViewStyle viewStyle = new ViewStyle();

    public static class ViewStyle{
        public final ObservableInt titleTextColor = new ObservableInt();
    }

    //点击事件
    public ReplyCommand itemClickCommand = new ReplyCommand(new Action0() {
        @Override
        public void call() {
            Toast.makeText(context,"点击了条目"+ title.get(),Toast.LENGTH_SHORT).show();
        }
    });

    public NewItemViewModel(Context context ,News.StoriesBean storiesBean){
        this.context = context;
        this.storiesBean = storiesBean;
        this.viewStyle.titleTextColor.set(context.getResources().getColor(android.R.color.black));
        if (storiesBean.getExtraField()!=null){
            date.set(NewsListHelper.changeDateFormat(storiesBean.getExtraField().getDate(), NewsListHelper.DAY_FORMAT, NewsListHelper.DAY_UI_FORMAT));
        }else{
            title.set(storiesBean.getTitle());
            imageUrl.set(storiesBean.getImages().get(0));
        }
    }
}
