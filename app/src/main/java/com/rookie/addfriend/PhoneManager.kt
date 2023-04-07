package com.rookie.addfriend

import java.util.LinkedList
import java.util.Queue


/*
 *  @项目名：  AddFriend 
 *  @包名：    com.rookie.addfriend
 *  @文件名:   PhoneManager
 *  @创建者:   rookietree
 *  @创建时间:  2023/3/18 00:46
 *  @描述：    TODO
 */
object PhoneManager {

    interface IAddChangedListener {
        fun onAddChanged()
    }

    var addListener: IAddChangedListener? = null
    var currentIndex: Int = 1

    var contactList: MutableList<ContactUser> = mutableListOf()

    val hasAddFinish: Boolean
        get() {
            return currentIndex >= contactList.size
        }

    fun resetIndex() {
        currentIndex = 1
    }

    fun getCurrentUser(): ContactUser? {
        return if (currentIndex < contactList.size) {
            contactList[currentIndex]
        } else {
            null
        }
    }

    fun addChange() {
        currentIndex++
        addListener?.onAddChanged()
    }

//    fun setAddListener(addChangeListener: IAddChangedListener) {
//        this.addListener = addChangeListener
//    }

}