package me.blog.korn123.easyphotomap.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_licenses.*
import me.blog.korn123.easyphotomap.R

/**
 * Created by CHO HANJOONG on 2017-02-11.
 */

class LicensesActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)
        licenses.loadUrl("https://github.com/hanjoongcho/aaf-easyphotomap/blob/master/LICENSE.md")
    }

}
