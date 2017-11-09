package me.blog.korn123.easyphotomap.activities

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import me.blog.korn123.easyphotomap.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by Administrator on 2017-11-09.
 */

@RunWith(AndroidJUnit4::class)
class IntroActivityTest {

    @Suppress("unused") // actually used by Espresso
    val activityRule @Rule get() = object :
            ActivityTestRule<IntroActivity>(IntroActivity::class.java) {
        override fun beforeActivityLaunched() {
        }
    }

    @Test fun test01() {
        onView(withId(R.id.companyName)).check(matches(isDisplayed()))
    }

    @Test fun test02() {
        onView(withId(R.id.companyName)).check(matches(withText("Awesome Application Factory")))
    }

}