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

    var contacts: Queue<ContactUser> = LinkedList()

    //记录是否添加过
    private var phoneMap = mutableMapOf<String, Int>()

    fun addPhoneNumber(str: String) {
        val numbers = str.split(" ")
        contacts.clear()
        for (number in numbers) {
            contacts.add(ContactUser(number))
            phoneMap[number] = 0
        }
    }

    fun hasClickOneRecent(number: String): Boolean {
        return phoneMap[number] == 1
    }

    fun hasClickTwoRecent(number: String): Boolean {
        return phoneMap[number] == 2
    }

    fun setHasOneRecent(number: String) {
        phoneMap[number] = 1
    }

    fun setHasTwoRecent(number: String) {
        phoneMap[number] = 2
    }

}