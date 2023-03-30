package com.rookie.addfriend

import android.app.Application
import com.chad.library.BuildConfig
import com.tencent.bugly.crashreport.CrashReport


/*
 *  @项目名：  AddFriend 
 *  @包名：    com.rookie.addfriend
 *  @文件名:   AddFriendApp
 *  @创建者:   rookietree
 *  @创建时间:  2023/3/17 15:18
 *  @描述：
 */
class AddFriendApp:Application() {

    override fun onCreate() {
        super.onCreate()
        CrashReport.initCrashReport(
            applicationContext,
            Constants.BUGLY_APP_ID,
            BuildConfig.DEBUG
        )
    }
}