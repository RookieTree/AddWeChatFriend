package com.rookie.addfriend


/*
 *  @项目名：  AddFriend 
 *  @包名：    com.rookie.addfriend
 *  @文件名:   ContactUser
 *  @创建者:   rookietree
 *  @创建时间:  2023/3/19 18:11
 *  @描述：    TODO
 */
data class ContactUser(
    val userPhone: String,
    val userName: String? = "",
    val helloWord: String? = ""
)
