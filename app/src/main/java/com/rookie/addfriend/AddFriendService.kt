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
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import java.lang.ref.WeakReference


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
            "android.widget.LinearLayout"  // 异常ui

        const val HOME_SEARCH_ICON_ID = "gsl"  // 首页-搜索框图标
        const val HOME_CHAT_TAB_ID = "kd_"  // 首页-微信tab
        const val HOME_SEARCH_EDIT_ID = "cd7"  // 搜索框
        const val HOME_SEARCH_RESULT_ID = "j54"  // 搜索结果
        const val ADD_CONTACT_BUTTON_ID = "khj"  // 添加到通讯录按钮

        const val ADD_SAYHI_ID = "j0w"  // 申请消息
        const val ADD_NAME_ID = "j0z"  // 备注edit
        const val ADD_SEND_ID = "e9q"  // 添加二级页-发送按钮

        const val ADD_MSG_CODE = 100
    }

    private var overlayView: View? = null
    private var tvIndex: TextView? = null
    private var mWindowManager: WindowManager? = null

    //是否开始添加好友
    var isStartAdd = false

    //添加好友的次数
    var addCount = 0

    class ScheduleHandler constructor(looper: Looper, addFriendService: AddFriendService) :
        Handler(looper) {
        private val weakReference = WeakReference(addFriendService)
        override fun handleMessage(msg: Message) {
            val addFriendService = weakReference.get() ?: return
            logD("ScheduleHandler service listening:$addFriendService")
            if (msg.what == ADD_MSG_CODE) {
                addFriendService.isStartAdd = true
                addFriendService.addCount = 0
                addFriendService.back()
                //一分钟后继续添加
                if (PhoneManager.hasAddFinish) {
                    addFriendService.isStartAdd = false
                    return
                }
                sendEmptyMessageDelayed(ADD_MSG_CODE, PhoneManager.addTimes)
            }
        }
    }

    var scheduleHandler: ScheduleHandler? = null

    override fun onCreate() {
        super.onCreate()
        // 创建Notification渠道，并开启前台服务
        createForegroundNotification()?.let { startForeground(1, it) }
        scheduleHandler = ScheduleHandler(Looper.myLooper()!!, this)
        scheduleHandler?.sendEmptyMessageDelayed(ADD_MSG_CODE, PhoneManager.addTimes)
        isStartAdd = true
        showWindow()
    }

    private fun showWindow() {
        // 获取 WindowManager
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        // 创建一个悬浮窗口 View
        overlayView = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.float_app_view,
            null
        ) as ConstraintLayout
        tvIndex = overlayView?.findViewById(R.id.tv_index)
        // 设置悬浮窗口参数
        val flag = (
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                )
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flag,
            PixelFormat.TRANSLUCENT
        )
        // 设置窗口布局的位置和大小
        params.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        // 将悬浮窗口 View 添加到 WindowManager 中
        mWindowManager?.addView(overlayView, params)
    }

    /**
     * 监听窗口变化的回调
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !isStartAdd) {
            return
        }
        //如果大于单次添加最大值，就停止
        if (addCount >= PhoneManager.addCountMax) {
            isStartAdd = false
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
            }
        }
    }

    override fun onInterrupt() {

    }

    private fun gotoSearch(event: AccessibilityEvent) {
        if (PhoneManager.contactList.isEmpty() || PhoneManager.hasAddFinish) {
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
        if (PhoneManager.contactList.isEmpty() || PhoneManager.hasAddFinish) {
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
            PhoneManager.getCurrentUser()?.helloWord?.let {
                sayHiView?.input(it)
            }
            PhoneManager.getCurrentUser()?.userName?.let {
                nameView?.input(it)
            }
            sleep(200)
            sendView.click()
            sleep(500)
            PhoneManager.currentIndex++
            refreshTvIndex()
            addCount++
//            gestureClick(source.getNodeByText("发送", true)?.parent)
            repeat(2) {
                back()
                sleep(200)
            }
        }
    }

    private fun refreshTvIndex() {
        if (PhoneManager.hasAddFinish) {
            tvIndex?.text = "已添加完"
        } else {
            tvIndex?.text =
                "正在添加${PhoneManager.currentIndex}/${PhoneManager.contactList.size - 1}位好友\n请勿操作手机"
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

    private fun createForegroundNotification(): Notification? {
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

    override fun onDestroy() {
        stopForeground(true)
        mWindowManager?.removeView(overlayView)
        super.onDestroy()
    }

}