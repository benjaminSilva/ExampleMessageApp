package com.bsoftwares.chatexample.model

data class LatestChatMessage(
    val messageID: String = "",
    val text: String = "",
    val myId: String = "",
    val otherId: String = "",
    val timeStamp: Long = 0,
    val fromUserName: String = "",
    val profilePhotoURL: String = ""
)

