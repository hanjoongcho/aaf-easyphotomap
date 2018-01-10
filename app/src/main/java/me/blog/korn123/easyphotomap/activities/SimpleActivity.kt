package me.blog.korn123.easyphotomap.activities

import io.github.hanjoongcho.commons.activities.BaseSimpleActivity
import me.blog.korn123.easyphotomap.extensions.initTextSize

/**
 * Created by CHO HANJOONG on 2018-01-08.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

open class SimpleActivity : BaseSimpleActivity() {

    override fun onResume() {
        super.onResume()
        initTextSize(findViewById(android.R.id.content), this@SimpleActivity);
    }
}