package me.blog.korn123.easyphotomap.activities

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.isBlackAndWhiteTheme
import com.simplemobiletools.commons.models.RadioItem
import io.github.hanjoongcho.commons.helpers.TransitionHelper
import kotlinx.android.synthetic.main.activity_settings.*
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.extensions.config
import me.blog.korn123.easyphotomap.extensions.initTextSize
import me.blog.korn123.easyphotomap.helper.FONT_SIZE_EXTRA_LARGE
import me.blog.korn123.easyphotomap.helper.FONT_SIZE_LARGE
import me.blog.korn123.easyphotomap.helper.FONT_SIZE_MEDIUM
import me.blog.korn123.easyphotomap.helper.FONT_SIZE_SMALL

/**
 * Created by CHO HANJOONG on 2018-01-09.
 */

class SettingsActivity : SimpleActivity() {
    lateinit var res: Resources
    private var linkColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        res = resources
        linkColor = if (isBlackAndWhiteTheme()) Color.WHITE else baseConfig.primaryColor

        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.settings)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onResume() {
        super.onResume()

        // handle option click
        setupCameraInfoPopup()
        setupDateFilter()
        setupAbout()
        setupFontSize()
    }

    private fun setupCameraInfoPopup() {
        disable_info_popup_label.setTextColor(linkColor)
        disable_info_popup_switcher.isChecked = config.disableCameraInformation
        disable_info_popup_holder.setOnClickListener {
            disable_info_popup_switcher.toggle()
            config.disableCameraInformation = disable_info_popup_switcher.isChecked
        }
        
    }
    
    private fun setupDateFilter() {
        date_filter_label.setTextColor(linkColor)
        date_filter_switcher.isChecked = config.enableDateFilter 
        date_filter_holder.setOnClickListener {
            date_filter_switcher.toggle()
            config.enableDateFilter = date_filter_switcher.isChecked
        }
    }
    
    private fun setupAbout() {
        about_label.setTextColor(linkColor)
        about_holder.setOnClickListener {
            TransitionHelper.startActivityWithTransition(this@SettingsActivity, AboutActivity::class.java)
        }
    }

    private fun setupFontSize() {
        settings_font_size.text = getFontSizeText()
        settings_font_size_holder.setOnClickListener {
            val items = arrayListOf(
                    RadioItem(FONT_SIZE_SMALL, res.getString(R.string.small)),
                    RadioItem(FONT_SIZE_MEDIUM, res.getString(R.string.medium)),
                    RadioItem(FONT_SIZE_LARGE, res.getString(R.string.large)),
                    RadioItem(FONT_SIZE_EXTRA_LARGE, res.getString(R.string.extra_large)))

            RadioGroupDialog(this@SettingsActivity, items, config.fontSize) {
                config.fontSize = it as Int
                settings_font_size.text = getFontSizeText()
//                updateWidget()
                initTextSize(findViewById(android.R.id.content), this@SettingsActivity);
            }
        }
        settings_font_size_label.setTextColor(linkColor)
    }

    private fun getFontSizeText() = getString(when (config.fontSize) {
        FONT_SIZE_SMALL -> R.string.small
        FONT_SIZE_MEDIUM -> R.string.medium
        FONT_SIZE_LARGE -> R.string.large
        else -> R.string.extra_large
    })
}