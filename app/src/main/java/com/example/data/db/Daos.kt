package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FarmFieldEntity
import com.example.data.model.FarmTaskEntity
import com.example.data.model.GuideEntity
import com.example.data.model.UserAccountEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveAccount(): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_accounts WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveAccountDirect(): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun findAccountByEmail(email: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UserAccountEntity): Long

    @Update
    suspend fun updateAccount(account: UserAccountEntity)

    @Query("UPDATE user_accounts SET isLoggedIn = 0")
    suspend fun clearActiveSessions()

    @Query("UPDATE user_accounts SET isLoggedIn = 1 WHERE id = :id")
    suspend fun setActiveSession(id: Long)

    @Query("UPDATE user_accounts SET isSetupCompleted = :isCompleted WHERE id = :id")
    suspend fun setSetupCompleted(id: Long, isCompleted: Boolean)

    @Query("DELETE FROM user_accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)

    @Query("DELETE FROM user_accounts")
    suspend fun deleteAllAccounts()
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    fun getUserProfileByUserId(userId: Long): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfileByUserIdDirect(userId: Long): UserProfileEntity?

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getFirstUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profile WHERE userId = :userId")
    suspend fun deleteUserProfileByUserId(userId: Long)

    @Query("DELETE FROM user_profile")
    suspend fun deleteAllUserProfiles()
}

@Dao
interface FarmFieldDao {
    @Query("SELECT * FROM farm_fields WHERE userId = :userId ORDER BY id ASC")
    fun getFieldsByUserId(userId: Long): Flow<List<FarmFieldEntity>>

    @Query("SELECT * FROM farm_fields ORDER BY id ASC")
    fun getAllFields(): Flow<List<FarmFieldEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertField(field: FarmFieldEntity): Long

    @Delete
    suspend fun deleteField(field: FarmFieldEntity)

    @Query("DELETE FROM farm_fields WHERE id = :id")
    suspend fun deleteFieldById(id: Long)

    @Query("DELETE FROM farm_fields WHERE userId = :userId")
    suspend fun deleteFieldsByUserId(userId: Long)

    @Query("DELETE FROM farm_fields")
    suspend fun deleteAllFields()
}

@Dao
interface FarmTaskDao {
    @Query("SELECT * FROM farm_tasks WHERE userId = :userId ORDER BY isCompleted ASC, id DESC")
    fun getTasksByUserId(userId: Long): Flow<List<FarmTaskEntity>>

    @Query("SELECT * FROM farm_tasks ORDER BY isCompleted ASC, id DESC")
    fun getAllTasks(): Flow<List<FarmTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: FarmTaskEntity): Long

    @Update
    suspend fun updateTask(task: FarmTaskEntity)

    @Query("DELETE FROM farm_tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("DELETE FROM farm_tasks WHERE userId = :userId")
    suspend fun deleteTasksByUserId(userId: Long)

    @Query("DELETE FROM farm_tasks")
    suspend fun deleteAllTasks()
}

@Dao
interface GuideDao {
    @Query("SELECT * FROM guides")
    fun getAllGuides(): Flow<List<GuideEntity>>

    @Query("SELECT * FROM guides WHERE category = :category")
    fun getGuidesByCategory(category: String): Flow<List<GuideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuides(guides: List<GuideEntity>)

    @Query("SELECT COUNT(*) FROM guides")
    suspend fun getGuidesCount(): Int
}
