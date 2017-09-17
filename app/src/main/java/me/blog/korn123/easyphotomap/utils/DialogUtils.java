package me.blog.korn123.easyphotomap.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.activities.FileExplorerActivity;
import me.blog.korn123.easyphotomap.activities.ThumbnailExplorerActivity;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class DialogUtils {

    public static void makeToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void makeSnackBar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    public static void showAlertDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        };
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlertDialog(Context context, String message, Activity activity, String imagePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        };
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 5;
        Bitmap originBitmap = BitmapUtils.decodeFile(activity, imagePath, options);
        Bitmap scaledBitmap = BitmapUtils.createScaledBitmap(originBitmap, CommonUtils.getDefaultDisplay(activity));
        ImageView thumbView = new ImageView(activity);
        thumbView.setImageBitmap(scaledBitmap);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(thumbView);
        builder.setView(layout);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlertDialog(Context context, String message, Activity activity, String imagePath, ThumbnailExplorerActivity.PositiveListener listener) {
        long start = System.currentTimeMillis();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final ThumbnailExplorerActivity.PositiveListener fLisntener = listener;
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fLisntener.register();
                return;
            }
        };
        DialogInterface.OnClickListener NegativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        };
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener);
        builder.setNegativeButton(context.getString(R.string.cancel), NegativeListener);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 5;
        Bitmap originBitmap = BitmapUtils.decodeFile(activity, imagePath, options);
        Bitmap scaledBitmap = BitmapUtils.createScaledBitmap(originBitmap, CommonUtils.getDefaultDisplay(activity));
        ImageView thumbView = new ImageView(activity);
        thumbView.setImageBitmap(scaledBitmap);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(thumbView);
        builder.setView(layout);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlertDialog(Context context, String message, Activity activity, FileExplorerActivity.PositiveListener listener) {
        long start = System.currentTimeMillis();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final FileExplorerActivity.PositiveListener fLisntener = listener;
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fLisntener.register();
                return;
            }
        };
        DialogInterface.OnClickListener NegativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        };
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener);
        builder.setNegativeButton(context.getString(R.string.cancel), NegativeListener);
        BitmapFactory.Options options = new BitmapFactory.Options();
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlertDialog(Context context, String message, Activity activity, String imagePath, FileExplorerActivity.PositiveListener listener) {
        long start = System.currentTimeMillis();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final FileExplorerActivity.PositiveListener fLisntener = listener;
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fLisntener.register();
                return;
            }
        };
        DialogInterface.OnClickListener NegativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        };
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener);
        builder.setNegativeButton(context.getString(R.string.cancel), NegativeListener);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 5;
        Bitmap originBitmap = BitmapUtils.decodeFile(activity, imagePath, options);
        Bitmap scaledBitmap = BitmapUtils.createScaledBitmap(originBitmap, CommonUtils.getDefaultDisplay(activity));
        ImageView thumbView = new ImageView(activity);
        thumbView.setImageBitmap(scaledBitmap);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(thumbView);
        builder.setView(layout);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlertDialog(Context context,
                                       String message,
                                       DialogInterface.OnClickListener positiveListener,
                                       DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
