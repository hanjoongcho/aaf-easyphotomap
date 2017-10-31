package me.blog.korn123.easyphotomap.activities

import org.apache.commons.lang.StringUtils
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.regex.Pattern

/**
 * Created by Administrator on 2017-10-31.
 */

class MapsActivityTest {
    @Test fun match() {
        val pattern = "[0-9]{1,9}"
        val regexString = "^($pattern-$pattern)|$pattern$"
        val address = "CA 94043 United States 1"
        val addresses = StringUtils.split(address, " ")
        val result = addresses.filter { Pattern.matches(regexString, it) }
        assertTrue(result.count() == 2 && result.first() == "94043")
    }
}