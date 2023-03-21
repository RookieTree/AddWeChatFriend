package com.rookie.addfriend

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.view.accessibility.AccessibilityEvent
import cn.coderpig.clearcorpse.*
import java.lang.Thread.sleep
import kotlin.math.log


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
        const val CONTACT_USER_SETTING_UI =
            "com.tencent.mm.plugin.profile.ui.ProfileSettingUI"  // 添加一级页副页
        const val ADD_CONTACT_USER_UI =
            "com.tencent.mm.plugin.profile.ui.SayHiWithSnsPermissionUI"  // 添加页二级页
        const val ADD_CONTACT_USER_SECOND_UI =
            "android.widget.LinearLayout"  // 添加页二级页-副

        const val HOME_SEARCH_ICON_ID = "com.tencent.mm:id/gsl"  // 首页-搜索框图标
        const val HOME_CHAT_TAB_ID = "com.tencent.mm:id/kd_"  // 首页-微信tab
        const val HOME_SEARCH_EDIT_ID = "com.tencent.mm:id/cd7"  // 搜索框
        const val HOME_SEARCH_RESULT_ID = "com.tencent.mm:id/j54"  // 搜索结果

        const val ADD_CONTACT_MORE_ID = "com.tencent.mm:id/by3"  // 添加一级页-省略号按钮
        const val ADD_CONTACT_BUTTON_ID = "com.tencent.mm:id/khj"  // 添加到通讯录按钮

        //        const val ADD_CONTACT_BUTTON_ID = "com.tencent.mm:id/iwg"  // 添加到通讯录按钮
        const val ADD_TXT_ID = "com.tencent.mm:id/j0w"  // 申请消息
        const val ADD_NAME_ID = "com.tencent.mm:id/j0z"  // 备注edit
        const val ADD_SEND_ID = "com.tencent.mm:id/e9q"  // 添加二级页-发送按钮
        const val ADD_SCROLLVIEW_ID = "com.tencent.mm:id/j3t"  // 添加二级页-滚动view
    }

    var handler = Handler()

    var hasAddFinish: Boolean = false

    /**
     * 服务链接的回调
     */
    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    var currentUser: ContactUser? = null

    /**
     * 监听窗口变化的回调
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) {
            return
        }
        logD("event_name:$event")
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            when (event.className.toString()) {
                LAUNCHER_UI -> {
                    if (PhoneManager.phoneNums.isEmpty()) {
                        return
                    }
                    event.source?.let { source ->
                        //先点到微信tab
                        source.getNodeById(HOME_CHAT_TAB_ID).click()
                        //点击搜索图标
                        source.getNodeById(HOME_SEARCH_ICON_ID).click()
                    }
                }
                SEARCH_UI -> {
                    if (PhoneManager.phoneNums.isEmpty()) {
                        return
                    }
                    event.source?.let { source ->
                        //让搜索框输入
                        val editView = source.getNodeById(HOME_SEARCH_EDIT_ID)
                        currentUser = PhoneManager.phoneNums.poll()
                        currentUser?.let { editView?.input(it.userPhone) }
                        sleep(200)
                        source.getNodeById(HOME_SEARCH_RESULT_ID).click()
                    }
                }
                CONTACT_USER_UI -> {
                    if (hasAddFinish) {
                        return
                    }
//                    sleep(200)
                    //点击添加通讯录
//                    addContactFirstPage2(event)
                    addContactFirstPage1(event)
//                    addContactFirstPage(event)
                }
                ADD_CONTACT_USER_UI -> {
                    if (hasAddFinish) {
                        return
                    }
//                    sleep(200)
                    addContactSecondPage2(event)
//                    addContactSecondPage1(event)
//                    addContactSecondPage(event)
                }
            }
        } else if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            logD("event_click_name:" + event.text)
            if (event.text.size == 0) {
                return
            }
//            val tvStr = event.text[0]
        }else if (event.eventType ==  AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            if (event.parcelableData != null && event.parcelableData is Notification) {
                val notification = event.parcelableData as Notification
                val content = notification.tickerText.toString()
                if (content.contains("我通过了你的朋友验证请求")) {
                    val pendingIntent = notification.contentIntent
                    try {
                        pendingIntent.send()
                    } catch (e: PendingIntent.CanceledException) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }

    private fun addContactSecondPage2(event: AccessibilityEvent) {
        event.source?.let { source ->
            source.getNodeById(ADD_TXT_ID)?.input("你好呀")
            source.getNodeById(ADD_NAME_ID)?.input("下雨不愁")
            sleep(200)
            source.getNodeById(ADD_SEND_ID).click()
            hasAddFinish = PhoneManager.phoneNums.isEmpty()
            repeat(2){
                back()
                sleep(200)
            }
        }
    }

    private fun addContactFirstPage2(event: AccessibilityEvent) {
        event.source?.let { source ->
            sleep(200)
            source.getNodeById(ADD_CONTACT_BUTTON_ID).click()
        }
    }

    private fun addContactSecondPage1(event: AccessibilityEvent) {
        event.source?.let { source ->
            source.getNodeById(ADD_TXT_ID)?.input("你好呀")
            gestureClick(source.getNodeByText("发送", true)?.parent)
            hasAddFinish = PhoneManager.phoneNums.isEmpty()
            repeat(2) {
                back()
                sleep(200)
            }
        }
    }

    private fun addContactFirstPage1(event: AccessibilityEvent) {
        event.source?.let { source ->
            gestureClick(source.getNodeByText("添加到通讯录", true)?.parent)
        }
    }

    private fun addContactFirstPage(event: AccessibilityEvent) {
        val hasRecent = PhoneManager.hasClickOneRecent(currentUser!!.userPhone)
        event.source?.let {
            if (hasRecent) {
                event.source!!.getNodeById(ADD_CONTACT_BUTTON_ID)?.click()
            } else {
                recentTask()
                sleep(200)
                back()
                PhoneManager.setHasOneRecent(currentUser!!.userPhone)
            }
        }
    }

    private fun addContactSecondPage(event: AccessibilityEvent) {
        val hasRecent = PhoneManager.hasClickTwoRecent(currentUser!!.userPhone)
        event.source?.let { source ->
            if (hasRecent) {
                //设置申请消息
                source.getNodeById(ADD_TXT_ID)?.input("你好呀")
                sleep(200)
                source.getNodeById(ADD_SEND_ID).click()
                hasAddFinish = PhoneManager.phoneNums.isEmpty()
                repeat(2) {
                    back()
                    sleep(200)
                }
            } else {
                recentTask()
                sleep(200)
                back()
                PhoneManager.setHasTwoRecent(currentUser!!.userPhone)
            }
        }
    }

    /**
     * 中断服务的回调
     */
    override fun onInterrupt() {
        TODO("Not yet implemented")
    }

    /**
     * 服务解绑回调
     */
    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

}