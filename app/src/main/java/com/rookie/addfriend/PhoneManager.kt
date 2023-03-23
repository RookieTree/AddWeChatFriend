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
    var contactList: MutableList<ContactUser> = mutableListOf()

    fun addPhoneNumber(str: String) {
        val numbers = str.split(" ")
        contacts.clear()
        for (number in numbers) {
            contacts.add(ContactUser(number))
        }
    }
}