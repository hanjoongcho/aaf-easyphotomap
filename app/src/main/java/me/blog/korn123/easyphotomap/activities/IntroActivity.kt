package me.blog.korn123.easyphotomap.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.helper.START_MAIN_ACTIVITY

/**
 * Created by CHO HANJOONG on 2016-12-31.
 */

class IntroActivity : Activity(), Handler.Callback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        Handler(this).sendEmptyMessageDelayed(START_MAIN_ACTIVITY, 1000)
    }

    override fun handleMessage(message: Message): Boolean {
        when (message.what) {
            START_MAIN_ACTIVITY -> {
                startActivity(Intent(this, MapsActivity::class.java))
                finish()
            }
            else -> {
            }
        }
        return false
    }
}
