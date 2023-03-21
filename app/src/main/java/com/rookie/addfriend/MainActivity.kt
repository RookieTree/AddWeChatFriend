package com.rookie.addfriend

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import cn.coderpig.clearcorpse.isAccessibilitySettingsOn
import cn.coderpig.clearcorpse.shortToast
import cn.coderpig.clearcorpse.startApp
import java.io.File


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
    private lateinit var btnClear: Button
    private lateinit var etPhones: EditText
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
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
        etPhones = findViewById(R.id.et_phone)
        btnAdd = findViewById(R.id.btn_add)
        btnClear = findViewById(R.id.btn_clear)
        btnAdd.setOnClickListener {
            /*val phones = etPhones.text.toString()
            if (TextUtils.isEmpty(phones)) {
//                shortToast("请输入手机号")
//                return@setOnClickListener
//                PhoneManager.addPhoneNumber("15112378930")
                PhoneManager.addPhoneNumber("17343371792")
            } else {
                PhoneManager.addPhoneNumber(phones)
            }*/
            /*if (!isAccessibilitySettingsOn(AddFriendService::class.java)) {
                showAccessDialog()
            } else {
                startApp("com.tencent.mm", "com.tencent.mm.ui.LauncherUI", "未安装微信")
//                startApp("weixin://", "未安装微信")
            }*/
            openFileSelector()
        }
        btnClear.setOnClickListener {
            etPhones.text.clear()
        }
    }

    // 打开系统自带的文件选择器
    private fun openFileSelector() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
//        intent.type = "application/vnd.ms-excel application/x-excel" 未知无效原因
        activityResultLauncher.launch(intent)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            // 用户未选择任何文件，直接返回
            return
        }
        val uri: Uri? = data.data // 获取用户选择文件的URI
        uri?.let {
            val file = FileUtil.getFilePathByUri(this, it)?.let { it1 -> File(it1) }
            if (ExcelUtils.checkIfExcelFile(file)) {
                ExcelUtils.readExcel(file) //读取Excel file 内容
            }
        }
    }

    var dialog: Dialog? = null
    override fun onResume() {
        super.onResume()
        if (!isAccessibilitySettingsOn(AddFriendService::class.java)) {
            showAccessDialog()
        } else {
            dialog?.dismiss()
        }
    }

    private fun showAccessDialog() {
        if (dialog == null) {
            dialog =
                AlertDialog.Builder(this).setTitle("请打开<<${this.getString(R.string.acc_des)}>>无障碍服务")
                    .setPositiveButton(
                        "确认"
                    ) { dialog, which ->
                        dialog.dismiss()
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }.create()
        }
//        dialog!!.show()
    }

    private fun dealSelectFile(it: ActivityResult) {
        if (it.data == null) {
            // 用户未选择任何文件，直接返回
            return
        }
        val uri: Uri? = it.data!!.data // 获取用户选择文件的URI
        uri?.let {
            val file = FileUtil.getFilePathByUri(this, it)?.let { it1 -> File(it1) }
            if (ExcelUtils.checkIfExcelFile(file)) {
                ExcelUtils.readExcel(file) //读取Excel file 内容
            }
        }
        return
    }

}