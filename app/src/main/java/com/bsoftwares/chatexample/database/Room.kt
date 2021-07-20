package com.bsoftwares.chatexample.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLatestMessages(vararg settings : LatestMessageDB)

    @Query("select * from latestmessagedb")
    fun getLatestMessages() : LiveData<List<LatestMessageDB>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertImages(vararg settings : UsersDB)

    @Query("select * from usersdb")
    fun getImagesList() : LiveData<List<UsersDB>>

    @Query("select * from usersdb WHERE userUID= :userUid")
    fun getUserUid(userUid : String?) : LiveData<UsersDB>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserirMessages(vararg settings : ChatMessageDB)

    @Query("select * from chatmessagedb WHERE chatId= :chatUid")
    fun getMessagesWithThisUser(chatUid : String) : LiveData<List<ChatMessageDB>>
}

@Database(entities = [ChatMessageDB::class,LatestMessageDB::class,UsersDB::class], version = 1)
@TypeConverters(Converters::class)
abstract class ChatDataBase : RoomDatabase() {
    abstract val dao : ChatDao
}

private lateinit var INSTANCE : ChatDataBase

fun getDataBase(context: Context) : ChatDataBase{
    if(!::INSTANCE.isInitialized)
        INSTANCE = Room.databaseBuilder(context.applicationContext,
            ChatDataBase::class.java,
            "ChatMessageDB").build()
    return INSTANCE
}
