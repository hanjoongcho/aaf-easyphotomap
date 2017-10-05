package me.blog.korn123.easyphotomap.helper

import java.util.ArrayList

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.Sort
import me.blog.korn123.easyphotomap.models.PhotoMapItem

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
            return Realm.getInstance(sDiaryConfig!!)
        }

    fun insertPhotoMapItem(photoMapItem: PhotoMapItem) {
        val realm = realmInstance
        realm.beginTransaction()
        var sequence = 1
        if (realm.where(PhotoMapItem::class.java).count() > 0) {
            val number = realm.where(PhotoMapItem::class.java).max("sequence")
            sequence = number.toInt() + 1
        }
        photoMapItem.sequence = sequence
        realm.insert(photoMapItem!!)
        realm.commitTransaction()
    }

    fun selectPhotoMapItemAll(): ArrayList<PhotoMapItem> {
        val realmResults = realmInstance.where(PhotoMapItem::class.java).findAllSorted("sequence", Sort.DESCENDING)
        val list = ArrayList<PhotoMapItem>()
        list.addAll(realmResults.subList(0, realmResults.size))
        return list
    }

    fun selectTimeLineItemAll(excludeDate: String): ArrayList<PhotoMapItem> {
        val realm = realmInstance
        val realmResults = realm.where(PhotoMapItem::class.java).notEqualTo("date", excludeDate).findAllSorted("date", Sort.ASCENDING)
        val list = ArrayList<PhotoMapItem>()
        list.addAll(realmResults.subList(0, realmResults.size))
        realm.beginTransaction()
        for (item in list) {
            item.dateWithoutTime = getSimpleDate(item.date!!)
        }
        realm.commitTransaction()
        return list
    }

    private fun getSimpleDate(date: String): String {
        var simpleDate = when(date.contains("(")) {
            true -> date.substring(0, date.lastIndexOf("("))
            false -> date
        }
        return simpleDate
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
        val realm = realmInstance
        val item = realm.where(PhotoMapItem::class.java).equalTo("sequence", sequence).findFirst()
        if (item != null) {
            realm.beginTransaction()
            item.deleteFromRealm()
            realm.commitTransaction()
        }
    }

}
