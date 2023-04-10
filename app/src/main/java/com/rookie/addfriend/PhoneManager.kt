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

    const val ADD_COUNT_MAX_DEFAULT = 1
    const val ADD_TIMES_DEFAULT = 30L

    interface IAddChangedListener {
        fun onAddChanged()
    }

    var addListener: IAddChangedListener? = null
    var currentIndex: Int = 1

    //添加朋友频率
    var addTimes = 1000 * ADD_TIMES_DEFAULT

    //周期内添加好友数
    var addCountMax = ADD_COUNT_MAX_DEFAULT

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

}