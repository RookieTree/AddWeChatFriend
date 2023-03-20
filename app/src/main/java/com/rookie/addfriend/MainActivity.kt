package com.rookie.addfriend

import android.app.Dialog
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import cn.coderpig.clearcorpse.isAccessibilitySettingsOn
import cn.coderpig.clearcorpse.shortToast
import cn.coderpig.clearcorpse.startApp


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
    override fun init() {
        etPhones = findViewById(R.id.et_phone)
        btnAdd = findViewById(R.id.btn_add)
        btnClear = findViewById(R.id.btn_clear)
        btnAdd.setOnClickListener {
            val phones = etPhones.text.toString()
            if (TextUtils.isEmpty(phones)) {
//                shortToast("请输入手机号")
//                return@setOnClickListener
//                PhoneManager.addPhoneNumber("15112378930")
                PhoneManager.addPhoneNumber("17343371792")
            } else {
                PhoneManager.addPhoneNumber(phones)
            }
            if (!isAccessibilitySettingsOn(AddFriendService::class.java)) {
                showAccessDialog()
            } else {
                startApp("com.tencent.mm", "com.tencent.mm.ui.LauncherUI", "未安装微信")
//                startApp("weixin://", "未安装微信")
            }
        }
        btnClear.setOnClickListener {
            etPhones.text.clear()
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
            dialog = AlertDialog.Builder(this).setTitle("请打开无障碍服务").setPositiveButton(
                "确认"
            ) { dialog, which ->
                dialog.dismiss()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }.create()
        }
        dialog!!.show()
    }

}