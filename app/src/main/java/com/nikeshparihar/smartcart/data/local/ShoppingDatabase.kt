package com.nikeshparihar.smartcart.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nikeshparihar.smartcart.data.local.entity.ShoppingListEntity
import com.nikeshparihar.smartcart.data.local.entity.ShoppingItemEntity
import com.nikeshparihar.smartcart.data.local.dao.ShoppingDao

@Database(
    entities = [ShoppingListEntity::class, ShoppingItemEntity::class],
    version = 3,
    exportSchema = false
)
abstract class ShoppingDatabase : RoomDatabase() {
    abstract fun shoppingDao(): ShoppingDao

    companion object {
        @Volatile
        private var INSTANCE: ShoppingDatabase? = null

        fun getDatabase(context: Context): ShoppingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShoppingDatabase::class.java,
                    "shopping_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
