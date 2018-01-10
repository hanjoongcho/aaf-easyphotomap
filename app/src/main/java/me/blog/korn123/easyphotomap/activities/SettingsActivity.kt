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
import me.blog.korn123.easyphotomap.helper.*

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
        setupPhotoMarkerScale()
        setupPhotoMarker()
        setupReversOrder()
        setupPhotoCopy()
        setupCameraInfoPopup()
        setupDateFilter()
        setupAbout()
        setupFontSize()
    }
    
    private fun setupPhotoMarkerScale() {
        photo_marker_scale_label.setTextColor(linkColor)
        photo_marker_scale_holder.setOnClickListener {  }    
    }
    
    private fun setupPhotoMarker() {
        photo_marker_label.setTextColor(linkColor)
        photo_marker_label.text = "${getString(R.string.photo_marker_setting)} (${getPhotoMarkerText()})"
        photo_marker_holder.setOnClickListener {
            val items = arrayListOf(
                    RadioItem(BASIC, res.getString(R.string.photo_marker_basic)),
                    RadioItem(FILM, res.getString(R.string.photo_marker_film)),
                    RadioItem(FLOWER, res.getString(R.string.photo_marker_flower)),
                    RadioItem(CIRCLE, res.getString(R.string.photo_marker_circle)))

            RadioGroupDialog(this@SettingsActivity, items, config.photoMarkerIcon) {
                config.photoMarkerIcon = it as Int
                photo_marker_label.text = "${getString(R.string.photo_marker_setting)} (${getPhotoMarkerText()})"
//                updateWidget()
            }
        }
    }
    
    private fun setupReversOrder() {
        enable_reverse_order_label.setTextColor(linkColor)
        enable_reverse_order_switcher.isChecked = config.enableReverseOrder
        enable_reverse_order_holder.setOnClickListener {
            enable_reverse_order_switcher.toggle()
            config.enableReverseOrder = enable_reverse_order_switcher.isChecked
        }
    }
    
    private fun setupPhotoCopy() {
        enable_copy_label.setTextColor(linkColor)
        enable_copy_switcher.isChecked = config.enableCreateCopy
        enable_copy_holder.setOnClickListener {
            enable_copy_switcher.toggle()
            config.enableCreateCopy = enable_copy_switcher.isChecked
        }
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
    
    private fun getPhotoMarkerText() = getString(when (config.photoMarkerIcon) {
        FILM -> R.string.photo_marker_film
        FLOWER -> R.string.photo_marker_flower
        CIRCLE -> R.string.photo_marker_circle
        else -> R.string.photo_marker_basic
    })
}