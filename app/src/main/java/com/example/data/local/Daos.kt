package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    fun getProjectById(id: Long): Flow<ProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: Long)
}

@Dao
interface ShortDao {
    @Query("SELECT * FROM shorts ORDER BY createdAt DESC")
    fun getAllShorts(): Flow<List<ShortEntity>>

    @Query("SELECT * FROM shorts WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getShortsByProject(projectId: Long): Flow<List<ShortEntity>>

    @Query("SELECT * FROM shorts WHERE id = :id LIMIT 1")
    fun getShortById(id: Long): Flow<ShortEntity?>

    @Query("SELECT * FROM shorts WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteShorts(): Flow<List<ShortEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShort(short: ShortEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShorts(shorts: List<ShortEntity>)

    @Update
    suspend fun updateShort(short: ShortEntity)

    @Query("DELETE FROM shorts WHERE id = :id")
    suspend fun deleteShort(id: Long)

    @Query("DELETE FROM shorts WHERE projectId = :projectId")
    suspend fun deleteShortsByProject(projectId: Long)

    @Query("UPDATE shorts SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE shorts SET status = :status, exportedPath = :exportedPath WHERE id = :id")
    suspend fun markExported(id: Long, status: String, exportedPath: String)
}

@Dao
interface CreditDao {
    @Query("SELECT * FROM credit_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<CreditTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CreditTransactionEntity): Long
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_account WHERE id = 1 LIMIT 1")
    fun getUserAccount(): Flow<UserAccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserAccountEntity)

    @Query("UPDATE user_account SET creditsUsed = :used WHERE id = 1")
    suspend fun updateCreditsUsed(used: Int)

    @Query("UPDATE user_account SET planId = :planId, creditsTotal = :creditsTotal WHERE id = 1")
    suspend fun updatePlan(planId: String, creditsTotal: Int)
}

@Dao
interface ProcessingJobDao {
    @Query("SELECT * FROM processing_jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<ProcessingJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: ProcessingJobEntity): Long

    @Update
    suspend fun updateJob(job: ProcessingJobEntity)

    @Query("DELETE FROM processing_jobs WHERE id = :id")
    suspend fun deleteJob(id: Long)
}

@Dao
interface TranscriptDao {
    @Query("SELECT * FROM transcripts WHERE projectId = :projectId ORDER BY createdAt DESC LIMIT 1")
    fun getTranscriptByProjectId(projectId: Long): Flow<TranscriptEntity?>

    @Query("SELECT * FROM transcripts WHERE id = :id LIMIT 1")
    fun getTranscriptById(id: Long): Flow<TranscriptEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscript(transcript: TranscriptEntity): Long

    @Query("DELETE FROM transcripts WHERE projectId = :projectId")
    suspend fun deleteTranscriptByProjectId(projectId: Long)
}
