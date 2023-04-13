package com.rookie.addfriend

import android.widget.TextView
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
        val tvPhone = holder.getView<TextView>(R.id.tvPhone)
        val tvName = holder.getView<TextView>(R.id.tvName)
        val tvSayHi = holder.getView<TextView>(R.id.tvSayHi)
        val itemPosition = getItemPosition(item)
        if (itemPosition%2==0) {
            tvPhone.background=null
            tvName.background=null
            tvSayHi.background=null
        } else {
            tvPhone.setBackgroundColor(context.getColor(R.color.color_dedede))
            tvName.setBackgroundColor(context.getColor(R.color.color_dedede))
            tvSayHi.setBackgroundColor(context.getColor(R.color.color_dedede))
        }
        if (itemPosition==0) {
            tvPhone.setTextColor(context.getColor(R.color.color_aaaaaa))
            tvName.setTextColor(context.getColor(R.color.color_aaaaaa))
            tvSayHi.setTextColor(context.getColor(R.color.color_aaaaaa))
        }else{
            tvPhone.setTextColor(context.getColor(R.color.black))
            tvName.setTextColor(context.getColor(R.color.black))
            tvSayHi.setTextColor(context.getColor(R.color.black))
        }
        item.userPhone.let {
            tvPhone.text = it
        }
        item.userName?.let {
            tvName.text = it
        }
        item.helloWord?.let {
            tvSayHi.text = it
        }
    }
}