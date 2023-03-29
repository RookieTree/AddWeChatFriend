package com.rookie.addfriend

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.PermissionUtils.SimpleCallback
import com.blankj.utilcode.util.UriUtils


/*
 *  @项目名：  AddFriend 
 *  @包名：    com.rookie.addfriend
 *  @文件名:   MainActivity
 *  @创建者:   rookietree
 *  @创建时间:  2023/3/17 15:15
 *  @描述：    TODO
 */
class MainActivity : BaseActivity() {
    override fun getLayoutID(): Int = R.layout.activity_main

    private lateinit var btnAdd: Button
    private lateinit var btnRead: Button
    private lateinit var rv: RecyclerView
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    var dialog: Dialog? = null
    var contactAdapter: ContactAdapter? = null

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
    }

    override fun init() {
        btnRead = findViewById(R.id.btn_read)
        btnAdd = findViewById(R.id.btn_add)
        rv = findViewById(R.id.rv)
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
            if (!isAccessibilitySettingsOn(AddFriendService::class.java)) {
                showAccessDialog()
            } else {
                PhoneManager.resetIndex()
                startWeChat()
            }
        }
    }

    private fun startWeChat() {
        startApp("com.tencent.mm", "com.tencent.mm.ui.LauncherUI", "未安装微信")
    }

    private fun checkReadPermissions() {
        if (!PermissionUtils.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionUtils.permission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .callback(object : SimpleCallback {
                    override fun onGranted() {
                        openFileSelector()
                    }

                    override fun onDenied() {

                    }

                })
                .request()
        } else {
            openFileSelector()
        }
    }

    // 打开系统自带的文件选择器
    private fun openFileSelector() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
//        intent.type = "*/*"
        intent.type = "application/vnd.ms-excel"
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//授予临时权限别忘
        activityResultLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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
            dialog =
                AlertDialog.Builder(this)
                    .setTitle("请打开<<${this.getString(R.string.acc_des)}>>无障碍服务")
                    .setPositiveButton(
                        "确认"
                    ) { dialog, which ->
                        dialog.dismiss()
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
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
}