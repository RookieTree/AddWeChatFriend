package com.rookie.addfriend

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder


/*
 *  @项目名：  AddFriend 
 *  @包名：    com.rookie.addfriend
 *  @文件名:   ContactAdapter
 *  @创建者:   rookietree
 *  @创建时间:  2023/3/23 11:36
 *  @描述：
 */
class ContactAdapter : BaseQuickAdapter<ContactUser, BaseViewHolder>(R.layout.item_contact) {
    override fun convert(holder: BaseViewHolder, item: ContactUser) {
        item.userPhone.let {
            holder.setText(R.id.tvPhone, it)
        }
        item.userName?.let {
            holder.setText(R.id.tvName, it)
        }
        item.helloWord?.let {
            holder.setText(R.id.tvSayHi, it)
        }

    }
}