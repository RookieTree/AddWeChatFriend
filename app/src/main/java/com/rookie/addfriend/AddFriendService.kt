package com.rookie.addfriend

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.ProcessUtils


/*
 *  @项目名：  AddFriend 
 *  @包名：    com.rookie.addfriend
 *  @文件名:   AddFriendService
 *  @创建者:   rookietree
 *  @创建时间:  2023/3/17 15:34
 *  @描述：    TODO
 */
class AddFriendService : AccessibilityService() {

    companion object {
        const val LAUNCHER_UI = "com.tencent.mm.ui.LauncherUI"  // 首页
        const val SEARCH_UI = "com.tencent.mm.plugin.fts.ui.FTSMainUI"  // 搜索页
        const val CONTACT_USER_UI = "com.tencent.mm.plugin.profile.ui.ContactInfoUI"  // 添加一级页
        const val ADD_CONTACT_USER_UI =
            "com.tencent.mm.plugin.profile.ui.SayHiWithSnsPermissionUI"  // 添加页二级页
        const val ADD_CONTACT_USER_SECOND_UI =
            "android.widget.LinearLayout"  // 存在ui id找不到

        const val HOME_SEARCH_ICON_ID = "gsl"  // 首页-搜索框图标
        const val HOME_CHAT_TAB_ID = "kd_"  // 首页-微信tab
        const val HOME_SEARCH_EDIT_ID = "cd7"  // 搜索框
        const val HOME_SEARCH_RESULT_ID = "j54"  // 搜索结果
        const val ADD_CONTACT_BUTTON_ID = "khj"  // 添加到通讯录按钮

        const val ADD_SAYHI_ID = "j0w"  // 申请消息
        const val ADD_NAME_ID = "j0z"  // 备注edit
        const val ADD_SEND_ID = "e9q"  // 添加二级页-发送按钮
    }

    override fun onCreate() {
        super.onCreate()
        // 创建Notification渠道，并开启前台服务
        createForegroundNotification()?.let { startForeground(1, it) }
    }

    /**
     * 服务链接的回调
     */
    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 监听窗口变化的回调
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.packageName != "com.tencent.mm") {
            return
        }
        logD("event_name:$event")
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            when (event.className.toString()) {
                LAUNCHER_UI -> {
                    gotoSearch(event)
                }
                SEARCH_UI -> {
                    searchPhone(event)
                }
                CONTACT_USER_UI -> {
                    //点击添加通讯录
                    addContactFirstPage(event)
                }
                ADD_CONTACT_USER_UI -> {
                    addContactSecondPage(event)
                }
//                ADD_CONTACT_USER_SECOND_UI -> {
//                    if (hasStepIntoAddFirst) {
//                        refreshTask()
//                        hasStepIntoAddFirst = false
//                    }
//                }
            }
        }
    }

    private fun gotoSearch(event: AccessibilityEvent) {
        if (PhoneManager.contactList.isEmpty()) {
            return
        }
        event.source?.let { source ->
            val tabView = source.getNodeById(wxNodeId(HOME_CHAT_TAB_ID))
            val searchView = source.getNodeById(wxNodeId(HOME_SEARCH_ICON_ID))
            //先点到微信tab
            tabView?.click()
            //点击搜索图标
            searchView?.click()
        }
    }

    private fun searchPhone(event: AccessibilityEvent) {
        if (PhoneManager.contactList.isEmpty()) {
            return
        }
        event.source?.let { source ->
            //让搜索框输入
            val editView = source.getNodeById(wxNodeId(HOME_SEARCH_EDIT_ID))
            PhoneManager.getCurrentUser()?.let { editView?.input(it.userPhone) }
            sleep(200)
            val searchResult = source.getNodeById(
                wxNodeId(HOME_SEARCH_RESULT_ID)
            )
            gestureClick(searchResult?.parent)
            searchResult.click()
        }
    }

    private fun addContactSecondPage(event: AccessibilityEvent) {
        if (PhoneManager.hasAddFinish) {
            logD("hasAddFinish")
            return
        }
        event.source?.let { source ->
            sleep(200)
            val sayHiView = source.getNodeById(wxNodeId(ADD_SAYHI_ID))
            val nameView = source.getNodeById(wxNodeId(ADD_NAME_ID))
            val sendView = source.getNodeById(wxNodeId(ADD_SEND_ID))
            logD("sayHiView:$sayHiView")
//            if (sayHiView == null || nameView == null || sendView == null) {
//                refreshTask()
//                return
//            }
            PhoneManager.getCurrentUser()?.helloWord?.let {
                sayHiView?.input(it)
            }
            PhoneManager.getCurrentUser()?.userName?.let {
                nameView?.input(it)
            }
            sleep(200)
            sendView.click()
            PhoneManager.currentIndex++
//            gestureClick(source.getNodeByText("发送", true)?.parent)
            repeat(2) {
                back()
                sleep(200)
            }
        }
    }

    private fun addContactFirstPage(event: AccessibilityEvent) {
        if (PhoneManager.hasAddFinish) {
            return
        }
        event.source?.let { source ->
            sleep(200)
            source.getNodeById(wxNodeId(ADD_CONTACT_BUTTON_ID)).click()
            gestureClick(source.getNodeByText("添加到通讯录", true)?.parent)
        }
    }

    /**
     * 中断服务的回调
     */
    override fun onInterrupt() {
    }

    /**
     * 服务解绑回调
     */
    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    private fun createForegroundNotification(): Notification? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            // 创建通知渠道，一定要写在创建显示通知之前，创建通知渠道的代码只有在第一次执行才会创建
            // 以后每次执行创建代码检测到该渠道已存在，因此不会重复创建
            val channelId = "add_friend"
            notificationManager?.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "添加好友",
                    NotificationManager.IMPORTANCE_HIGH // 发送通知的等级，此处为高
                )
            )
            return NotificationCompat.Builder(this, channelId)
                // 设置点击notification跳转，比如跳转到设置页
                .setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        0,
                        Intent(this, MainActivity::class.java),
                        FLAG_IMMUTABLE
                    )
                )
                .setSmallIcon(R.drawable.ic_app) // 设置小图标
                .setContentTitle(getString(R.string.acc_des))
                .setContentText("添加好友")
                .setTicker("添加好友")
                .build()
        }
        return null
    }

    override fun onDestroy() {
        stopForeground(true)
        super.onDestroy()
    }

}