package me.blog.korn123.easyphotomap.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;

import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.utils.BitmapUtils;
import me.blog.korn123.easyphotomap.utils.CommonUtils;

/**
 * Created by CHO HANJOONG on 2016-08-21.
 */
public class PopupImageActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_image);
        ImageView imageView = (ImageView)findViewById(R.id.imageView);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 5;
        String imagePath = getIntent().getStringExtra("imagePath");
        Bitmap bitmap = null;
        Bitmap targetBitmap = null;
        if (new File(imagePath).exists()) {
            BitmapUtils.INSTANCE.decodeFile(PopupImageActivity.this, imagePath, options);
            int width = options.outWidth;
            int height = options.outHeight;
            options.inJustDecodeBounds = false;
            bitmap = BitmapUtils.INSTANCE.decodeFile(PopupImageActivity.this, imagePath, options);
            if ((width > height && CommonUtils.getDisplayOrientation(this) == 0) ||
                    (width < height && CommonUtils.getDisplayOrientation(this) == 1)) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                targetBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            } else {
                targetBitmap = bitmap;
            }
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_gallery);
            targetBitmap = bitmap;
        }

        imageView.setImageBitmap(targetBitmap);

        ImageButton finish = (ImageButton)findViewById(R.id.finish);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
