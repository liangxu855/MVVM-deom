package com.example.administrator.mvvm_deom.utils;

import android.util.Log;

import com.example.administrator.mvvm_deom.retrofit.ApiException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.ReplaySubject;

/**
 * Created by Administrator on 2018-02-28.
 */

public class NewsListHelper {
    public static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat DAY_UI_FORMAT = new SimpleDateFormat("yyyy年MM月dd日");

    public static void dealWithResponseError(Observable<Throwable> throwableObservable) {
        ReplaySubject<Throwable> throwableReplaySubject = ReplaySubject.create();
        throwableObservable.subscribe(throwableReplaySubject);

        throwableReplaySubject
                .repeat(5)
                .scan(new Func2<Throwable, Throwable, Throwable>() {
                    @Override
                    public Throwable call(Throwable throwable, Throwable throwable2) {
                        return throwable.getCause();
                    }
                })
                .takeUntil(new Func1<Throwable, Boolean>() {
                    @Override
                    public Boolean call(Throwable throwable) {
                        return throwable.getCause() == null;
                    }
                })
                .filter(new Func1<Throwable, Boolean>() {
                    @Override
                    public Boolean call(Throwable throwable) {
                        return throwable instanceof ApiException;
                    }
                })
                .cast(ApiException.class)
                .subscribe(new Action1<ApiException>() {
                    @Override
                    public void call(ApiException e) {
                        Log.v("error", e.msg);
                    }
                });
    }

    public static boolean isTomorrow(String date) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat("yyyyMMdd").format(calendar.getTime()).equals(date);
    }

    public static String changeDateFormat(String oldDate, SimpleDateFormat oldFormat, SimpleDateFormat newFormat) {
        Date date;
        try {
            date = oldFormat.parse(oldDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return newFormat.format(date);
    }
}
