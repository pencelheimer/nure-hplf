package com.pz3.app.data.db

import androidx.room.*
import com.pz3.app.data.model.TodoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY id ASC")
    fun getAll(): Flow<List<TodoItem>>

    @Insert
    suspend fun insert(item: TodoItem)

    @Query("UPDATE todos SET done = :done WHERE id = :id")
    suspend fun setDone(id: Int, done: Boolean)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun delete(id: Int)
}
