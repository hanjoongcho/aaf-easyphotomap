# Easy Photo Map

[![Awesome](https://cdn.rawgit.com/sindresorhus/awesome/d7305f38d29fed78fa85652e3a63e154dd8e8829/media/badge.svg)](https://github.com/unicodeveloper/awesome-opensource-apps)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)
[![License][licensesvg]][LICENSE.md]   

<img src="screenshots/ic_launcher.png">  

Easy Photo Map is a photomap application that displays the location of the photo on the map using the location information included in the photo.  
Latest version can be download from the link below.  
<a href='https://play.google.com/store/apps/details?id=me.blog.korn123.easyphotomap'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="258" height="98"/></a><br />


# Screen Shot
<img src="screenshots/1.png" width="288" height="512">&nbsp;
<img src="screenshots/2.png" width="288" height="512">&nbsp;
<img src="screenshots/3.png" width="288" height="512">&nbsp;
<img src="screenshots/4.png" width="288" height="512">&nbsp;
<img src="screenshots/5.png" width="288" height="512">&nbsp;
<img src="screenshots/6.png" width="288" height="512">&nbsp;
<img src="screenshots/7.png" width="288" height="512">&nbsp;

# How to build
```
Step1. Fork or download 'aaf-easyphotomap' project.
Step2. Import 'aaf-easyphotomap' project into android studio.
Step3. Register your package name and SHA-1 signature certificate fingerprint for Google Map use.(https://console.developers.google.com/)
Step4. Define api key to 'google_maps_api.xml' file.
       - /app/src/release/res/values/google_maps_api.xml
Step5. Build 'aaf-easyphotomap' project with android studio.
```

# Project Tree
```
├─java
│  └─me
│      └─blog
│          └─korn123
│              └─easyphotomap
│                  ├─activities
│                  │      AddressSearchActivity.kt
│                  │      AppPreferenceActivity.kt
│                  │      BatchPopupActivity.kt
│                  │      CameraActivity.kt
│                  │      FileExplorerActivity.kt
│                  │      IntroActivity.kt
│                  │      MapsActivity.kt
│                  │      PhotoSearchActivity.kt
│                  │      PopupImageActivity.kt
│                  │      SettingsActivity.kt
│                  │      ThumbnailExplorerActivity.kt
│                  │      TimelineActivity.kt
│                  │      WebViewActivity.kt
│                  │
│                  ├─adapters
│                  │      AddressItemAdapter.kt
│                  │      ExplorerItemAdapter.kt
│                  │      SearchItemAdapter.kt
│                  │      ThumbnailItemAdapter.kt
│                  │      TimelineItemAdapter.kt
│                  │
│                  ├─constants
│                  │      Constant.kt
│                  │
│                  ├─helper
│                  │      EasyPhotoMapApplication.kt
│                  │      PhotoMapDbHelper.kt
│                  │      PhotoMapMigration.kt
│                  │      RegistrationThread.kt
│                  │
│                  ├─models
│                  │      FileItem.kt
│                  │      PhotoMapItem.kt
│                  │      ThumbnailItem.kt
│                  │
│                  ├─thumbnail
│                  │      AsyncDrawable.kt
│                  │      BitmapWorkerTask.kt
│                  │
│                  └─utils
│                          AsyncUtils.kt
│                          BitmapUtils.kt
│                          CommonUtils.kt
│                          DialogUtils.kt
│                          FontUtils.kt
│                          GPSUtils.kt
│
└─res
    ├─drawable
    │      bg_activitity_timeline.xml
    │      bg_recommendation_card.xml
    │      camera_info.png
    │      cell_shape.xml
    │      circle.xml
    │      circle2.xml
    │      circle3.xml
    │      dialog_bg.xml
    │      dialog_bg2.xml
    │      dialog_bg_primary.xml
    │      dialog_bg_primary_without_padding.xml
    │      frame_02.jpg
    │      frame_03.jpg
    │      ic_fast_scroll.xml
    │      ic_fast_scroll_default.xml
    │      ic_fast_scroll_press.xml
    │      intro.png
    │      selector_listview.xml
    │
    ├─drawable-hdpi
    │      bg_map_info.xml
    │      ic_camera.png
    │      ic_close.png
    │      ic_day_view.png
    │      ic_edit.png
    │      ic_folder.png
    │      ic_image.png
    │      ic_map_marker.png
    │      ic_marker.png
    │      ic_menu.png
    │      ic_menu_archive.png
    │      ic_menu_gallery.png
    │      ic_photos.png
    │      ic_progress.png
    │      ic_search.png
    │      ic_settings.png
    │      ic_timeline.png
    │      logout_w.png
    │
    ├─drawable-mdpi
    │      ic_camera.png
    │      ic_close.png
    │      ic_day_view.png
    │      ic_edit.png
    │      ic_folder.png
    │      ic_image.png
    │      ic_map_marker.png
    │      ic_marker.png
    │      ic_menu.png
    │      ic_menu_archive.png
    │      ic_menu_gallery.png
    │      ic_photos.png
    │      ic_progress.png
    │      ic_search.png
    │      ic_settings.png
    │      ic_timeline.png
    │
    ├─drawable-xhdpi
    │      bg_common.xml
    │      ic_camera.png
    │      ic_close.png
    │      ic_day_view.png
    │      ic_edit.png
    │      ic_folder.png
    │      ic_image.png
    │      ic_map_marker.png
    │      ic_marker.png
    │      ic_menu.png
    │      ic_menu_archive.png
    │      ic_menu_gallery.png
    │      ic_photos.png
    │      ic_progress.png
    │      ic_search.png
    │      ic_settings.png
    │      ic_timeline.png
    │      logout_w.png
    │
    ├─drawable-xxhdpi
    │      ic_camera.png
    │      ic_close.png
    │      ic_day_view.png
    │      ic_edit.png
    │      ic_folder.png
    │      ic_image.png
    │      ic_map_marker.png
    │      ic_marker.png
    │      ic_menu.png
    │      ic_menu_archive.png
    │      ic_menu_gallery.png
    │      ic_photos.png
    │      ic_progress.png
    │      ic_search.png
    │      ic_settings.png
    │      ic_timeline.png
    │      logout_w.png
    │
    ├─layout
    │      activity_address_search.xml
    │      activity_batch_popup.xml
    │      activity_camera.xml
    │      activity_file_explorer.xml
    │      activity_intro.xml
    │      activity_maps.xml
    │      activity_photo_search.xml
    │      activity_popup_image.xml
    │      activity_thumbnail_explorer.xml
    │      activity_timeline.xml
    │      activity_web_view.xml
    │      item_file_explorer.xml
    │      item_recommendation.xml
    │      item_search.xml
    │      item_thumbnail.xml
    │      item_timeline.xml
    │      popup_window_camera.xml
    │      popup_window_photo_map_info.xml
    │      popup_window_recommendation.xml
    │      preference_category.xml
    │
    ├─menu
    │      menu.xml
    │      setting_menu.xml
    │      thumbnail_explorer_menu.xml
    │
    ├─mipmap-hdpi
    │      ic_launcher.png
    │
    ├─mipmap-mdpi
    │      ic_launcher.png
    │
    ├─mipmap-xhdpi
    │      ic_launcher.png
    │
    ├─mipmap-xxhdpi
    │      ic_launcher.png
    │
    ├─mipmap-xxxhdpi
    │      ic_launcher.png
    │
    ├─values
    │      colors.xml
    │      dimens.xml
    │      strings.xml
    │      styles.xml
    │
    ├─values-en
    │      strings.xml
    │
    ├─values-ja
    │      strings.xml
    │
    ├─values-ko
    │      strings.xml
    │
    └─xml
            pref_general.xml
```

# License
[LICENSE][LICENSE.md]

[licensesvg]: https://img.shields.io/badge/License-Apache--2.0-brightgreen.svg
[LICENSE.md]: https://github.com/hanjoongcho/aaf-easyphotomap/blob/master/LICENSE.md