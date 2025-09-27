package com.example.tagriculture.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tagriculture.analytics.AnalyticsEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Database(entities = [Animal::class, Tag::class, WeightEntry::class, Notification::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animalDao(): AnimalDao
    abstract fun tagDao(): TagDao
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tagriculture_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(private val context: Context) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }

        private suspend fun populateDatabase(database: AppDatabase) {
            val animalDao = database.animalDao()
            val weightEntryDao = database.weightEntryDao()
            val notificationDao = database.notificationDao()

            if (animalDao.getAllAnimalsForSeeding().isNotEmpty()) return

            fun getDrawableUri(resourceName: String): String {
                return "android.resource://${context.packageName}/drawable/$resourceName"
            }

            suspend fun generateWeightHistory(
                animalId: Long, birthDate: Long, birthWeight: Double, currentWeight: Double, forceWeightDrop: Boolean = false
            ) {
                val now = System.currentTimeMillis()
                val totalLifeSpan = now - birthDate
                val numEntries = 5
                val random = java.util.Random(animalId)
                val weightGain = currentWeight - birthWeight
                val averageGainPerInterval = weightGain / numEntries
                var wiggle = 0.0

                for (i in 0..numEntries) {
                    val progress = i.toFloat() / numEntries
                    val entryDate = birthDate + (totalLifeSpan * progress).toLong()
                    wiggle = (averageGainPerInterval * 0.5) * (random.nextDouble() - 0.5)
                    val entryWeight = birthWeight + (averageGainPerInterval * i) + wiggle
                    weightEntryDao.insertWeightEntry(WeightEntry(
                        animalId = animalId,
                        weight = String.format(Locale.US, "%.1f", entryWeight).toDouble(),
                        date = entryDate
                    ))
                }

                if (forceWeightDrop) {
                    val lastGeneratedWeight = birthWeight + weightGain + wiggle
                    val lastWeight = lastGeneratedWeight - (lastGeneratedWeight * 0.05)
                    weightEntryDao.insertWeightEntry(WeightEntry(
                        animalId = animalId,
                        weight = String.format(Locale.US, "%.1f", lastWeight).toDouble(),
                        date = now + 1000
                    ))
                }
            }

            suspend fun generateNotificationsForAnimal(animal: Animal) {
                val history = weightEntryDao.getWeightHistoryForAnimalSync(animal.id)
                if (history.isEmpty()) return

                if (history.size >= 2 && history.last().weight < history[history.size - 2].weight) {
                    val notification = Notification(
                        animalId = animal.id, animalName = animal.name, alertType = "HEALTH",
                        message = "Weight has decreased since last measurement.",
                        timestamp = System.currentTimeMillis() + animal.id
                    )
                    notificationDao.insertNotification(notification)
                }

                val report = AnalyticsEngine.generateReport(animal, history)
                report.readinessAlerts.forEach { alertPair ->
                    val notification = Notification(
                        animalId = animal.id, animalName = animal.name, alertType = alertPair.first.name,
                        message = alertPair.second,
                        timestamp = System.currentTimeMillis() + animal.id
                    )
                    notificationDao.insertNotification(notification)
                }
            }

            var animalId: Long
            var currentAnimal: Animal

            // Listing 1: Berto
            val bertoBirthDate = calculateBirthDate(3.0)
            currentAnimal = Animal(animalType = "Cattle", name = "Berto", breed = "Philippine Native–Brahman Cross", birthDate = bertoBirthDate, birthWeight = 38.0, currentWeight = 420.0, locationCity = "Batangas City", locationMunicipal = "CALABARZON", pictureUri = getDrawableUri("listing_1"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, bertoBirthDate, 38.0, 420.0)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))

            // Listing 2: Kalbo
            val kalboBirthDate = calculateBirthDate(2.5)
            currentAnimal = Animal(animalType = "Cattle", name = "Kalbo", breed = "Philippine Native", birthDate = kalboBirthDate, birthWeight = 35.0, currentWeight = 350.0, locationCity = "San Fernando", locationMunicipal = "Pampanga", pictureUri = getDrawableUri("listing_2"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, kalboBirthDate, 35.0, 350.0, forceWeightDrop = true)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))

            // Listing 3: Sultan
            val sultanBirthDate = calculateBirthDate(3.5)
            currentAnimal = Animal(animalType = "Cattle", name = "Sultan", breed = "Brahman", birthDate = sultanBirthDate, birthWeight = 42.0, currentWeight = 550.0, locationCity = "General Santos City", locationMunicipal = "SOCCSKSARGEN", pictureUri = getDrawableUri("listing_3"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, sultanBirthDate, 42.0, 550.0)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))

            // Listing 4: Pogi
            val pogiBirthDate = calculateBirthDate(1.8)
            currentAnimal = Animal(animalType = "Cattle", name = "Pogi", breed = "Philippine Native", birthDate = pogiBirthDate, birthWeight = 30.0, currentWeight = 220.0, locationCity = "Tarlac City", locationMunicipal = "Central Luzon", pictureUri = getDrawableUri("listing_4"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, pogiBirthDate, 30.0, 220.0)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))

            // Listing 5: Bruno
            val brunoBirthDate = calculateBirthDate(4.0)
            currentAnimal = Animal(animalType = "Cattle", name = "Bruno", breed = "Ongole–Native Cross", birthDate = brunoBirthDate, birthWeight = 40.0, currentWeight = 450.0, locationCity = "Davao City", locationMunicipal = "Davao Region", pictureUri = getDrawableUri("listing_5"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, brunoBirthDate, 40.0, 450.0)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))

            // Listing 6: Snow
            val snowBirthDate = calculateBirthDate(1.5)
            currentAnimal = Animal(animalType = "Sheep", name = "Snow", breed = "Philippine Sheep (Katjang)", birthDate = snowBirthDate, birthWeight = 4.0, currentWeight = 38.0, locationCity = "La Trinidad", locationMunicipal = "Benguet", pictureUri = getDrawableUri("listing_6"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, snowBirthDate, 4.0, 38.0)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))

            // Listing 7: Nene
            val neneBirthDate = calculateBirthDate(2.0)
            currentAnimal = Animal(animalType = "Sheep", name = "Nene", breed = "Philippine Native Sheep", birthDate = neneBirthDate, birthWeight = 3.5, currentWeight = 30.0, locationCity = "Malaybalay", locationMunicipal = "Bukidnon", pictureUri = getDrawableUri("listing_7"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, neneBirthDate, 3.5, 30.0, forceWeightDrop = true)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))

            // Listing 8: Tisoy
            val tisoyBirthDate = calculateBirthDate(2.5)
            currentAnimal = Animal(animalType = "Goat", name = "Tisoy", breed = "Anglo-Nubian Cross", birthDate = tisoyBirthDate, birthWeight = 4.0, currentWeight = 42.0, locationCity = "Lucena City", locationMunicipal = "CALABARZON", pictureUri = getDrawableUri("listing_8"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, tisoyBirthDate, 4.0, 42.0)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))

            // Listing 9: Kikay
            val kikayBirthDate = calculateBirthDate(0.5)
            currentAnimal = Animal(animalType = "Goat", name = "Kikay", breed = "Philippine Native Goat", birthDate = kikayBirthDate, birthWeight = 2.0, currentWeight = 18.0, locationCity = "Tagbilaran City", locationMunicipal = "Bohol", pictureUri = getDrawableUri("listing_9"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, kikayBirthDate, 2.0, 18.0)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))

            // Listing 10: Blanca
            val blancaBirthDate = calculateBirthDate(3.0)
            currentAnimal = Animal(animalType = "Goat", name = "Blanca", breed = "Saanen Cross", birthDate = blancaBirthDate, birthWeight = 3.8, currentWeight = 40.0, locationCity = "Baybay City", locationMunicipal = "Leyte", pictureUri = getDrawableUri("listing_10"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, blancaBirthDate, 3.8, 40.0)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))

            // Listing 11: Pinky
            val pinkyBirthDate = calculateBirthDate(0.58)
            currentAnimal = Animal(animalType = "Pig", name = "Pinky", breed = "Philippine Native–Landrace Cross", birthDate = pinkyBirthDate, birthWeight = 1.5, currentWeight = 95.0, locationCity = "Cebu City", locationMunicipal = "Cebu", pictureUri = getDrawableUri("listing_11"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, pinkyBirthDate, 1.5, 95.0)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))

            // Listing 12: Linda
            val lindaBirthDate = calculateBirthDate(2.8)
            currentAnimal = Animal(animalType = "Pig", name = "Linda", breed = "Large White–Duroc Cross", birthDate = lindaBirthDate, birthWeight = 1.8, currentWeight = 160.0, locationCity = "Cagayan de Oro", locationMunicipal = "Misamis Oriental", pictureUri = getDrawableUri("listing_12"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, lindaBirthDate, 1.8, 160.0)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))

            // Listing 13: Apollo
            val apolloBirthDate = calculateBirthDate(5.0)
            currentAnimal = Animal(animalType = "Horse", name = "Apollo", breed = "Philippine Thoroughbred", birthDate = apolloBirthDate, birthWeight = 50.0, currentWeight = 490.0, locationCity = "Taguig City", locationMunicipal = "Metro Manila", pictureUri = getDrawableUri("listing_13"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, apolloBirthDate, 50.0, 490.0)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))

            // Listing 14: Kulas
            val kulasBirthDate = calculateBirthDate(6.0)
            currentAnimal = Animal(animalType = "Horse", name = "Kulas", breed = "Philippine Native Horse", birthDate = kulasBirthDate, birthWeight = 45.0, currentWeight = 310.0, locationCity = "Vigan City", locationMunicipal = "Ilocos Sur", pictureUri = getDrawableUri("listing_14"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, kulasBirthDate, 45.0, 310.0)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))

            // Listing 15: Baste
            val basteBirthDate = calculateBirthDate(4.0)
            currentAnimal = Animal(animalType = "Buffalo", name = "Baste", breed = "Philippine Carabao", birthDate = basteBirthDate, birthWeight = 40.0, currentWeight = 460.0, locationCity = "Roxas City", locationMunicipal = "Capiz", pictureUri = getDrawableUri("listing_15"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, basteBirthDate, 40.0, 460.0)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))

            // Listing 16: Milky
            val milkyBirthDate = calculateBirthDate(5.0)
            currentAnimal = Animal(animalType = "Buffalo", name = "Milky", breed = "Murrah–Carabao Cross", birthDate = milkyBirthDate, birthWeight = 45.0, currentWeight = 520.0, locationCity = "Nueva Ecija City", locationMunicipal = "Central Luzon", pictureUri = getDrawableUri("listing_16"))
            animalId = animalDao.insertAnimal(currentAnimal)
            generateWeightHistory(animalId, milkyBirthDate, 45.0, 520.0)
            generateNotificationsForAnimal(currentAnimal.copy(id = animalId))
        }

        private fun calculateBirthDate(ageInYears: Double): Long {
            val calendar = Calendar.getInstance()
            val monthsToSubtract = (ageInYears * 12.0).toInt()
            calendar.add(Calendar.MONTH, -monthsToSubtract)
            return calendar.timeInMillis
        }
    }
}