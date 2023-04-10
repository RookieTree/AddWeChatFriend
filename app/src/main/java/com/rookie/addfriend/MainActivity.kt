package com.rookie.addfriend

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.UriUtils
import permissions.dispatcher.ktx.PermissionsRequester
import permissions.dispatcher.ktx.constructPermissionsRequest
import permissions.dispatcher.ktx.constructSystemAlertWindowPermissionRequest


/*
 *  @项目名：  AddFriend 
 *  @包名：    com.rookie.addfriend
 *  @文件名:   MainActivity
 *  @创建者:   rookietree
 *  @创建时间:  2023/3/17 15:15
 *  @描述：
 */
class MainActivity : BaseActivity() {
    override fun getLayoutID(): Int = R.layout.activity_main

    private lateinit var btnAdd: Button
    private lateinit var btnRead: Button
    private lateinit var rv: RecyclerView
    private lateinit var etTime: EditText
    private lateinit var etCount: EditText
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var dialog: Dialog? = null
    var contactAdapter: ContactAdapter? = null
    private var readRequest: PermissionsRequester? = null
    private var systemAlertRequest: PermissionsRequester? = null
    private var mWindowManager: WindowManager? = null
    private var overlayView: View? = null
    private var tvIndex: TextView? = null
    private var addTime = PhoneManager.ADD_TIMES_DEFAULT
    private var addCount = PhoneManager.ADD_COUNT_MAX_DEFAULT

    companion object {
        const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                dealSelectFile(it)
            }
        }
        readRequest = constructPermissionsRequest(Manifest.permission.READ_EXTERNAL_STORAGE) {
            openFileSelector()
        }
        systemAlertRequest = constructSystemAlertWindowPermissionRequest() {
            showWindow()
            startApp("com.tencent.mm", "com.tencent.mm.ui.LauncherUI", "未安装微信")
        }
        PhoneManager.addListener = object : PhoneManager.IAddChangedListener {
            override fun onAddChanged() {
                if (PhoneManager.hasAddFinish) {
                    tvIndex?.text = "已添加完"
                } else {
                    tvIndex?.text =
                        "正在添加${PhoneManager.currentIndex}/${PhoneManager.contactList.size - 1}位好友\n请勿操作手机"
                }
            }
        }
    }

    private fun showWindow() {
        if (mWindowManager == null) {
            // 获取 WindowManager
            mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            // 创建一个悬浮窗口 View
            overlayView = View.inflate(this, R.layout.float_app_view, null)
            tvIndex = overlayView?.findViewById(R.id.tv_index)
            // 设置悬浮窗口参数
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            // 设置窗口布局的位置和大小
            params.gravity = Gravity.BOTTOM
            // 将悬浮窗口 View 添加到 WindowManager 中
            mWindowManager?.addView(overlayView, params)
        }
    }

    override fun init() {
        btnRead = findViewById(R.id.btn_read)
        btnAdd = findViewById(R.id.btn_add)
        rv = findViewById(R.id.rv)
        etTime = findViewById(R.id.et_time)
        etTime = findViewById(R.id.et_count)
        rv.layoutManager = LinearLayoutManager(this)
        contactAdapter = ContactAdapter()
        rv.adapter = contactAdapter
        btnRead.setOnClickListener {
            checkReadPermissions()
        }
        btnAdd.setOnClickListener {
            if (PhoneManager.contactList.isEmpty()) {
                shortToast("清先读取本地excel文件")
                return@setOnClickListener
            }
            if (addTime < PhoneManager.ADD_TIMES_DEFAULT) {
                ToastUtils.showShort("周期最低时间为30秒，请重新输入")
                return@setOnClickListener
            }
            if (addCount < PhoneManager.ADD_COUNT_MAX_DEFAULT) {
                ToastUtils.showShort("周期内最低添加个数为1，请重新输入")
                return@setOnClickListener
            }
            if (!isAccessibilitySettingsOn(AddFriendService::class.java)) {
                showAccessDialog()
            } else {
                PhoneManager.resetIndex()
                startWeChat()
            }
        }
        etTime.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event?.action == KeyEvent.ACTION_DOWN) {
                    addTime = etTime.text.toString().toLong()
                    if (addTime >= PhoneManager.ADD_TIMES_DEFAULT) {
                        PhoneManager.addTimes = addTime * 1000
                    }
                    return true
                }
                return false
            }
        })
        etCount.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event?.action == KeyEvent.ACTION_DOWN) {
                    addCount = etCount.text.toString().toInt()
                    if (addCount >= PhoneManager.ADD_COUNT_MAX_DEFAULT) {
                        PhoneManager.addCountMax = addCount
                    }
                    return true
                }
                return false
            }
        })
    }

    private fun startWeChat() {
        systemAlertRequest?.launch()
    }

    private fun checkReadPermissions() {
        readRequest?.launch()
    }

    // 打开系统自带的文件选择器
    private fun openFileSelector() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
//        intent.type = "application/vnd.ms-excel"
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//授予临时权限别忘
        activityResultLauncher.launch(Intent.createChooser(intent, "选择文件"))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openFileSelector()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isAccessibilitySettingsOn(AddFriendService::class.java)) {
            dialog?.dismiss()
        }
    }

    private fun showAccessDialog() {
        if (dialog == null) {
            dialog = AlertDialog.Builder(this)
                .setTitle("请打开<<${this.getString(R.string.acc_des)}>>无障碍服务").setPositiveButton(
                    "确认"
                ) { dialog, which ->
                    dialog.dismiss()
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                }.create()
        }
        dialog!!.show()
    }

    private fun dealSelectFile(it: ActivityResult) {
        if (it.data == null) {
            // 用户未选择任何文件，直接返回
            return
        }
        val uri: Uri? = it.data!!.data // 获取用户选择文件的URI
        uri?.let {
            val file = UriUtils.uri2File(it)
            file?.run {
                if (ExcelUtils.checkIfExcelFile(file)) {
                    ExcelUtils.readExcelInContact(file) //读取Excel file 内容
                    contactAdapter?.setNewInstance(PhoneManager.contactList)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mWindowManager?.removeView(overlayView)
    }
}