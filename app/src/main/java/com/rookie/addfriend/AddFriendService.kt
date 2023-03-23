package com.rookie.addfriend

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import cn.coderpig.clearcorpse.*
import java.lang.Thread.sleep


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

        const val HOME_SEARCH_ICON_ID = "gsl"  // 首页-搜索框图标
        const val HOME_CHAT_TAB_ID = "kd_"  // 首页-微信tab
        const val HOME_SEARCH_EDIT_ID = "cd7"  // 搜索框
        const val HOME_SEARCH_RESULT_ID = "j54"  // 搜索结果
        const val ADD_CONTACT_BUTTON_ID = "khj"  // 添加到通讯录按钮

        //        const val ADD_CONTACT_BUTTON_ID = "com.tencent.mm:id/iwg"  // 添加到通讯录按钮
        const val ADD_SAYHI_ID = "j0w"  // 申请消息
        const val ADD_NAME_ID = "j0z"  // 备注edit
        const val ADD_SEND_ID = "e9q"  // 添加二级页-发送按钮
        const val ADD_SCROLLVIEW_ID = "j3t"  // 添加二级页-滚动view
    }

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
                    if (PhoneManager.contacts.isEmpty()) {
                        return
                    }
                    event.source?.let { source ->
                        //先点到微信tab
                        source.getNodeById(wxNodeId(HOME_CHAT_TAB_ID)).click()
                        //点击搜索图标
                        source.getNodeById(wxNodeId(HOME_SEARCH_ICON_ID)).click()
                    }
                }
                SEARCH_UI -> {
                    if (PhoneManager.contacts.isEmpty()) {
                        return
                    }
                    event.source?.let { source ->
                        //让搜索框输入
                        val editView = source.getNodeById(wxNodeId(HOME_SEARCH_EDIT_ID))
                        currentUser = PhoneManager.contacts.poll()
                        currentUser?.let { editView?.input(it.userPhone) }
                        sleep(200)
                        source.getNodeById(wxNodeId(HOME_SEARCH_RESULT_ID)).click()
                    }
                }
                CONTACT_USER_UI -> {
                    if (hasAddFinish) {
                        return
                    }
                    //点击添加通讯录
                    addContactFirstPage(event)
                }
                ADD_CONTACT_USER_UI -> {
                    if (hasAddFinish) {
                        return
                    }
                    addContactSecondPage(event)
                }
            }
        }
    }

    private fun addContactSecondPage(event: AccessibilityEvent) {
        if (currentUser == null) {
            return
        }
        event.source?.let { source ->
            currentUser!!.helloWord?.let {
                source.getNodeById(wxNodeId(ADD_SAYHI_ID))?.input(it)
            }
            currentUser!!.userName?.let {
                source.getNodeById(wxNodeId(ADD_NAME_ID))?.input(it)
            }
//            sleep(200)
//            source.getNodeById(ADD_SEND_ID).click()
//            gestureClick(source.getNodeByText("发送", true)?.parent)
            hasAddFinish = PhoneManager.contacts.isEmpty()
            repeat(2) {
                back()
                sleep(200)
            }
        }
    }

    private fun addContactFirstPage(event: AccessibilityEvent) {
        event.source?.let { source ->
            sleep(200)
            source.getNodeById(wxNodeId(ADD_CONTACT_BUTTON_ID)).click()
//            gestureClick(source.getNodeByText("添加到通讯录", true)?.parent)
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

}