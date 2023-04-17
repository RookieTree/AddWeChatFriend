package com.rookie.addfriend


/*
 *  @项目名：  AddFriend 
 *  @包名：    com.rookie.addfriend
 *  @文件名:   PhoneManager
 *  @创建者:   rookietree
 *  @创建时间:  2023/3/18 00:46
 *  @描述：
 */
object PhoneManager {

    //默认10s
    const val ADD_TIMES_DEFAULT = 10

    interface IAddListener {
        fun onStartAdd()
    }

    var addListener: IAddListener? = null
    var currentIndex: Int = 1

    //添加朋友频率
    var addTimes = ADD_TIMES_DEFAULT

    var contactList = mutableListOf<ContactUser>()

    var contactFailList = mutableListOf<ContactUser>()

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

    fun startAdd() {
        addListener?.onStartAdd()
    }

}