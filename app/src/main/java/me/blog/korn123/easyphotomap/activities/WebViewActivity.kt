package me.blog.korn123.easyphotomap.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_web_view.*
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.constants.Constant

/**
 * Created by CHO HANJOONG on 2017-02-11.
 */

class WebViewActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        webView.loadUrl(intent.getStringExtra(Constant.OPEN_URL_INFO))

        finish.setOnClickListener({ finish() })
    }

}
