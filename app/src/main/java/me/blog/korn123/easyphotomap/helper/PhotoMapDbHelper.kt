package me.blog.korn123.easyphotomap.helper

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.Sort
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import java.util.*

/**
 * Created by CHO HANJOONG on 2017-09-12.
 */

object PhotoMapDbHelper {

    @Volatile private var sDiaryConfig: RealmConfiguration? = null

    private val realmInstance: Realm
        get() {
            if (sDiaryConfig == null) {
                sDiaryConfig = RealmConfiguration.Builder()
                        .name("easyphotomap.realm")
                        .schemaVersion(1)
                        .migration(PhotoMapMigration())
                        .modules(Realm.getDefaultModule())
                        .build()

            }
            return Realm.getInstance(sDiaryConfig)
        }

    fun insertPhotoMapItem(photoMapItem: PhotoMapItem) {
        realmInstance.beginTransaction()
        var sequence = 1
        realmInstance.where(PhotoMapItem::class.java)?.max("sequence")?.let { max ->
            sequence = max.toInt() + 1
        }
        photoMapItem.sequence = sequence
        realmInstance.insert(photoMapItem)
        realmInstance.commitTransaction()
    }

    fun selectPhotoMapItemAll(): ArrayList<PhotoMapItem> {
        val realmResults = realmInstance.where(PhotoMapItem::class.java).findAllSorted("sequence", Sort.DESCENDING)
        val list = ArrayList<PhotoMapItem>()
        list.addAll(realmResults.subList(0, realmResults.size))
        return list
    }

    fun selectTimeLineItemAll(excludeDate: String): ArrayList<PhotoMapItem> {
        val realmResults = realmInstance.where(PhotoMapItem::class.java).notEqualTo("date", excludeDate).findAllSorted("date", Sort.ASCENDING)
        val list = ArrayList<PhotoMapItem>()
        list.addAll(realmResults.subList(0, realmResults.size))
        realmInstance.beginTransaction()
        for (item in list) {
            item.dateWithoutTime = getSimpleDate(item.date)
        }
        realmInstance.commitTransaction()
        return list
    }

    private fun getSimpleDate(date: String): String = when(date.contains("(")) {
        true -> date.substring(0, date.lastIndexOf("("))
        false -> date
    }

    fun selectPhotoMapItemBy(targetColumn: String, value: String): ArrayList<PhotoMapItem> {
        val realmResults = realmInstance.where(PhotoMapItem::class.java).equalTo(targetColumn, value).findAllSorted("sequence", Sort.DESCENDING)
        val list = ArrayList<PhotoMapItem>()
        list.addAll(realmResults.subList(0, realmResults.size))
        return list
    }

    fun containsPhotoMapItemBy(targetColumn: String, value: String): ArrayList<PhotoMapItem> {
        val realmResults = realmInstance.where(PhotoMapItem::class.java).contains(targetColumn, value).findAllSorted("sequence", Sort.DESCENDING)
        val list = ArrayList<PhotoMapItem>()
        list.addAll(realmResults.subList(0, realmResults.size))
        return list
    }

    fun deletePhotoMapItemBy(sequence: Int) {
        val item: PhotoMapItem? = realmInstance.where(PhotoMapItem::class.java).equalTo("sequence", sequence).findFirst()
        item?.let {
            realmInstance.beginTransaction()
            it.deleteFromRealm()
            realmInstance.commitTransaction()
        }
    }

}
