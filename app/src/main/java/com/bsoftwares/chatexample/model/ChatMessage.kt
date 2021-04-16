package com.bsoftwares.chatexample.model

class ChatMessage(val id : String = "",val text : String = "", val fromID : String = "", val toID : String = "",val timeStamp : Long = -1,val position : Int = -1, val fromUserName : String = "",val sentToken : String = "")
