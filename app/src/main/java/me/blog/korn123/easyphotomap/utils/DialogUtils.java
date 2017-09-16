package me.blog.korn123.easyphotomap.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class DialogUtils {
    public static void makeSnackBar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    public static void showAlertDialog(Context context,
                                       String message,
                                       DialogInterface.OnClickListener positiveListener,
                                       DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setIcon(R.drawable.ic_launcher);
//        builder.setTitle("일기삭제");
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setNegativeButton("취소", negativeListener);
        builder.setPositiveButton("확인", positiveListener);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlertDialog(Context context,
                                       String message,
                                       DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton("확인", positiveListener);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlertDialog(Context context,
                                       String title,
                                       String message,
                                       DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
//        builder.setIcon(R.drawable.book);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton("확인", positiveListener);
        AlertDialog alert = builder.create();
        alert.show();
    }
}
