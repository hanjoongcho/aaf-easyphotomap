package me.blog.korn123.easyphotomap.activities

import android.support.test.runner.AndroidJUnit4
import org.apache.commons.lang.StringUtils
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern
import android.support.test.InstrumentationRegistry

/**
 * Created by Administrator on 2017-10-31.
 */

@RunWith(AndroidJUnit4::class)
class MapsActivityTest {

    @Test
    @Throws(Exception::class)
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()

        assertEquals("me.blog.korn123.easyphotomap", appContext.packageName)
    }

    @Test fun match() {
        val pattern = "[0-9]{1,9}"
        val regexString = "^($pattern-$pattern)|$pattern$"
        val address = "CA 94043 United States 1"
        val addresses = StringUtils.split(address, " ")
        val result = addresses.filter { Pattern.matches(regexString, it) }
        assertTrue(result.count() == 2 && result.first() == "94043")
    }

}