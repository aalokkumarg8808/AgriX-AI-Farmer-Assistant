package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.FarmFieldEntity
import com.example.data.model.FarmTaskEntity
import com.example.data.model.GuideEntity
import com.example.data.model.UserAccountEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserAccountEntity::class,
        UserProfileEntity::class,
        FarmFieldEntity::class,
        FarmTaskEntity::class,
        GuideEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AgriXDatabase : RoomDatabase() {

    abstract fun userAccountDao(): UserAccountDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun farmFieldDao(): FarmFieldDao
    abstract fun farmTaskDao(): FarmTaskDao
    abstract fun guideDao(): GuideDao

    companion object {
        @Volatile
        private var INSTANCE: AgriXDatabase? = null

        fun getDatabase(context: Context): AgriXDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgriXDatabase::class.java,
                    "agrix_database"
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { populateInitialData(it) }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AgriXDatabase) {
            // Seed essential agricultural learning guides
            val initialGuides = listOf(
                GuideEntity(
                    category = "Fertilizer",
                    titleEn = "Fertilizer & Seed Selection Guide",
                    titleHi = "उर्वरक एवं बीज चयन निर्देशिका",
                    titlePa = "ਖਾਦ ਅਤੇ ਬੀਜ ਚੋਣ ਗਾਈਡ",
                    contentEn = "Optimal application of Nitrogen, Phosphorus, and Potassium (NPK) balanced for seasonal crops. Ensure seed treatment with bio-fungicides before sowing.",
                    contentHi = "मौसमी फसलों के लिए नाइट्रोजन, फास्फोरस और पोटेशियम (एनपीके) का संतुलित उपयोग। बुआई से पहले जैव-फफूंदनाशी से बीजोपचार सुनिश्चित करें।",
                    contentPa = "ਮੌਸਮੀ ਫ਼ਸਲਾਂ ਲਈ ਨਾਈਟ੍ਰੋਜਨ, ਫਾਸਫੋਰਸ ਅਤੇ ਪੋਟਾਸ਼ੀਅਮ (ਐਨਪੀਕੇ) ਦੀ ਸੰਤੁਲਿਤ ਵਰਤੋਂ। ਬੀਜਣ ਤੋਂ ਪਹਿਲਾਂ ਜੈਵਿਕ ਫਫੁੰਦਨਾਸ਼ਕ ਨਾਲ ਬੀਜ ਦੀ ਸੋਧ ਯਕੀਨੀ ਬਣਾਓ।",
                    readTimeMinutes = 4
                ),
                GuideEntity(
                    category = "Crops",
                    titleEn = "Kharif Crop Management Tips",
                    titleHi = "खरीफ फसल प्रबंधन सुझाव",
                    titlePa = "ਖਰੀਫ ਫਸਲ ਪ੍ਰਬੰਧਨ ਸੁਝਾਅ",
                    contentEn = "Ensure proper field drainage during heavy monsoon rains. Monitor crop leaf color for nutrient deficiencies.",
                    contentHi = "भारी मानसून बारिश के दौरान खेतों में उचित जल निकासी सुनिश्चित करें। पोषक तत्वों की कमी के लिए फसल के पत्तों के रंग पर नज़र रखें।",
                    contentPa = "ਭਾਰੀ ਮਾਨਸੂਨ ਬਾਰਸ਼ ਦੌਰਾਨ ਖੇਤਾਂ ਵਿੱਚ ਪਾਣੀ ਦੇ ਨਿਕਾਸ ਦਾ ਸੁਚੱਜਾ ਪ੍ਰਬੰਧ ਕਰੋ। ਪੌਸ਼ਟਿਕ ਤੱਤਾਂ ਦੀ ਘਾਟ ਲਈ ਪੱਤਿਆਂ ਦੇ ਰੰਗ ਦੀ ਨਿਗਰਾਨੀ ਕਰੋ।",
                    readTimeMinutes = 3
                ),
                GuideEntity(
                    category = "Irrigation",
                    titleEn = "Drip & Precision Irrigation Methods",
                    titleHi = "ड्रिप और सूक्ष्म सिंचाई विधियां",
                    titlePa = "ਡ੍ਰਿਪ ਅਤੇ ਸ਼ੁੱਧ ਸਿੰਚਾਈ ਵਿਧੀਆਂ",
                    contentEn = "Save up to 40% water by irrigating during early morning or late evening. Maintain clean drip nozzles.",
                    contentHi = "सुबह जल्दी या देर शाम सिंचाई करके 40% तक पानी बचाएं। ड्रिप नोजल को साफ रखें।",
                    contentPa = "ਸਵੇਰੇ ਜਲਦੀ ਜਾਂ ਦੇਰ ਸ਼ਾਮ ਨੂੰ ਸਿੰਚਾਈ ਕਰਕੇ 40% ਤੱਕ ਪਾਣੀ ਬਚਾਓ। ਡ੍ਰਿਪ ਨੋਜ਼ਲਾਂ ਨੂੰ ਸਾਫ਼ ਰੱਖੋ।",
                    readTimeMinutes = 5
                ),
                GuideEntity(
                    category = "Pests",
                    titleEn = "Organic Pest Control Practices",
                    titleHi = "जैविक कीट नियंत्रण अभ्यास",
                    titlePa = "ਜੈਵਿਕ ਕੀੜੇ-ਮਕੌੜੇ ਰੋਕਥਾਮ",
                    contentEn = "Use Neem oil spray (5ml per liter) to manage early aphids and caterpillars without harming beneficial insects.",
                    contentHi = "मित्र कीटों को नुकसान पहुंचाए बिना शुरुआती माहू और सूंडियों के प्रबंधन के लिए नीम के तेल के घोल (5 मिली प्रति लीटर) का उपयोग करें।",
                    contentPa = "ਲਾਹੇਵੰਦ ਕੀੜਿਆਂ ਨੂੰ ਨੁਕਸਾਨ ਪਹੁੰਚਾਏ ਬਿਨਾਂ ਸ਼ੁਰੂਆਤੀ ਸੁੰਡੀਆਂ ਦੇ ਪ੍ਰਬੰਧਨ ਲਈ ਨਿੰਮ ਦੇ ਤੇਲ (5 ਮਿ.ਲੀ. ਪ੍ਰਤੀ ਲੀਟਰ) ਦੀ ਵਰਤੋਂ ਕਰੋ।",
                    readTimeMinutes = 4
                ),
                GuideEntity(
                    category = "Soil",
                    titleEn = "Understanding Soil Testing & pH",
                    titleHi = "मृदा परीक्षण एवं पीएच मान को समझना",
                    titlePa = "ਮਿੱਟੀ ਦੀ ਜਾਂਚ ਅਤੇ pH ਮਾਨ ਨੂੰ ਸਮਝਣਾ",
                    contentEn = "Test soil every 2 years. Maintain pH between 6.0 and 7.5 for maximum crop absorption of fertilizers.",
                    contentHi = "हर 2 साल में मिट्टी का परीक्षण कराएं। उर्वरकों के अधिकतम अवशोषण के लिए पीएच मान 6.0 और 7.5 के बीच बनाए रखें।",
                    contentPa = "ਹਰ 2 ਸਾਲ ਬਾਅਦ ਮਿੱਟੀ ਦੀ ਜਾਂਚ ਕਰਵਾਓ। ਖਾਦਾਂ ਦੀ ਵੱਧ ਤੋਂ ਵੱਧ ਵਰਤੋਂ ਲਈ pH 6.0 ਅਤੇ 7.5 ਵਿਚਕਾਰ ਰੱਖੋ।",
                    readTimeMinutes = 5
                )
            )
            db.guideDao().insertGuides(initialGuides)
        }
    }
}
