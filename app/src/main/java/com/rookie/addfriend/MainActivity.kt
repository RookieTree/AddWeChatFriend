package com.rookie.addfriend

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.core.widget.ContentLoadingProgressBar
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.UriUtils
import com.google.android.material.internal.TextWatcherAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private lateinit var tvError: TextView
    private lateinit var rv: RecyclerView
    private lateinit var rvError: RecyclerView
    private lateinit var etTime: EditText
    private lateinit var progress: ContentLoadingProgressBar
    private lateinit var tvTotal: TextView
    private lateinit var ivSetting: ImageView
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var dialog: Dialog? = null
    var contactAdapter: ContactAdapter? = null
    var contactErrorAdapter: ContactAdapter? = null
    private var readRequest: PermissionsRequester? = null
    private var systemAlertRequest: PermissionsRequester? = null

    companion object {
        const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
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
            if (!isAccessibilitySettingsOn(AddFriendService::class.java)) {
                showAccessDialog()
            } else {
                PhoneManager.resetIndex()
                startWeChat()
            }
        }
    }

    override fun init() {
        btnRead = findViewById(R.id.btn_read)
        btnAdd = findViewById(R.id.btn_add)
        tvError = findViewById(R.id.tv_error)
        rv = findViewById(R.id.rv)
        rvError = findViewById(R.id.rv_error)
        etTime = findViewById(R.id.et_time)
        progress = findViewById(R.id.progress)
        tvTotal = findViewById(R.id.tv_total)
        ivSetting = findViewById(R.id.iv_setting)
        val groupSetting = findViewById<Group>(R.id.group_setting)
        ivSetting.setOnClickListener {
            if (groupSetting.isVisible) {
                groupSetting.visibility = View.GONE
            } else {
                groupSetting.visibility = View.VISIBLE
            }
        }
        rv.layoutManager = LinearLayoutManager(this)
        rvError.layoutManager = LinearLayoutManager(this)
        contactAdapter = ContactAdapter()
        contactErrorAdapter = ContactAdapter()
        rv.adapter = contactAdapter
        rvError.adapter = contactErrorAdapter
        btnRead.setOnClickListener {
            checkReadPermissions()
        }
        btnAdd.setOnClickListener {
            if (PhoneManager.contactList.isEmpty()) {
                shortToast("清先读取本地excel文件")
                return@setOnClickListener
            }
            systemAlertRequest?.launch()
        }
        etTime.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val time = s?.toString()
                if (TextUtils.isEmpty(time)) {
                    PhoneManager.addTimes = PhoneManager.ADD_TIMES_DEFAULT
                } else {
                    time?.toInt()?.let {
                        checkTime(it)
                    }
                }
            }

        })
    }

    private fun checkTime(time: Int) {
        if (time >= PhoneManager.ADD_TIMES_DEFAULT) {
            PhoneManager.addTimes = time
        } else {
            ToastUtils.showShort("最少10s")
        }
    }

    private fun startWeChat() {
        startApp("com.tencent.mm", "com.tencent.mm.ui.LauncherUI", "未安装微信")
        PhoneManager.startAdd()
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
        if (PhoneManager.contactFailList.isNotEmpty()) {
            tvError.visibility = View.VISIBLE
            rvError.visibility = View.VISIBLE
            contactErrorAdapter?.setList(PhoneManager.contactFailList)
        } else {
            tvError.visibility = View.GONE
            rvError.visibility = View.GONE
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
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        val uri: Uri = it.data!!.data ?: return // 获取用户选择文件的URI
        val file = UriUtils.uri2File(uri) ?: return
        if (ExcelUtils.checkIfExcelFile(file)) {
            //读取Excel file 内容
            progress.visibility = View.VISIBLE
            coroutineScope.launch {
                val contacts = ExcelUtils.readExcelInContact(file)
                contacts.let {
                    PhoneManager.contactList.clear()
                    PhoneManager.contactList.addAll(it)
                    contactAdapter?.setList(PhoneManager.contactList)
                    tvTotal.text = "好友总数量: ${PhoneManager.contactList.size - 1}"
                    progress.visibility = View.GONE
                }
            }
        } else {
            ToastUtils.showShort("请选择excel文件")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_DOWN) {
            moveTaskToBack(true)
            return true;
        }
        return super.onKeyDown(keyCode, event)
    }
}