package com.example.tagriculture.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Database(entities = [Animal::class, Tag::class, WeightEntry::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animalDao(): AnimalDao
    abstract fun tagDao(): TagDao
    abstract fun weightEntryDao(): WeightEntryDao

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
                    // --- NEW: Add our callback here ---
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
            INSTANCE?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(getDatabase(context))
                }
            }
        }

        private suspend fun populateDatabase(database: AppDatabase) {
            val animalDao = database.animalDao()
            val weightEntryDao = database.weightEntryDao()

            // Check if the database is already populated
            if (animalDao.getAllAnimalsForSeeding().isNotEmpty()) return

            // --- MOCK DATA INSERTION ---

            // Helper function to create a resource URI string for drawables
            fun getDrawableUri(resourceName: String): String {
                return "android.resource://${context.packageName}/drawable/$resourceName"
            }

            // Helper to generate a weight history
            suspend fun generateWeightHistory(
                animalId: Long,
                birthDate: Long,
                birthWeight: Double,
                currentWeight: Double,
                forceWeightDrop: Boolean = false // New parameter to trigger health alerts
            ) {
                val now = System.currentTimeMillis()
                val totalLifeSpan = now - birthDate
                val numEntries = 5 // Create more data points
                val random = java.util.Random(animalId) // Seed with animalId for consistent randomness

                val weightGain = currentWeight - birthWeight
                val averageGainPerInterval = weightGain / numEntries

                for (i in 0..numEntries) {
                    val progress = i.toFloat() / numEntries
                    val entryDate = birthDate + (totalLifeSpan * progress).toLong()

                    // Calculate a base weight with some random oscillation
                    val wiggle = (averageGainPerInterval * 0.5) * (random.nextDouble() - 0.5) // +/- 25% of average gain
                    val entryWeight = birthWeight + (averageGainPerInterval * i) + wiggle

                    weightEntryDao.insertWeightEntry(WeightEntry(
                        animalId = animalId,
                        weight = String.format(Locale.US, "%.1f", entryWeight).toDouble(),
                        date = entryDate
                    ))
                }

                // If forced, add a final entry with a slightly lower weight to trigger the alert
                if (forceWeightDrop) {
                    val lastWeight = currentWeight - (currentWeight * 0.02) // A 2% drop
                    weightEntryDao.insertWeightEntry(WeightEntry(
                        animalId = animalId,
                        weight = String.format(Locale.US, "%.1f", lastWeight).toDouble(),
                        date = now + 1000 // Ensure it's the very last entry
                    ))
                }
            }

            var animalId: Long // Variable to hold the ID of the last inserted animal

            // Listing 1: Berto
            val bertoBirthDate = calculateBirthDate(3.0)
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Cattle", name = "Berto", breed = "Philippine Native–Brahman Cross",
                birthDate = bertoBirthDate, birthWeight = 38.0, currentWeight = 420.0,
                locationCity = "Batangas City", locationMunicipal = "CALABARZON",
                pictureUri = getDrawableUri("listing_1")
            ))
            generateWeightHistory(animalId, bertoBirthDate, 38.0, 420.0)

            // Listing 2: Kalbo
            val kalboBirthDate = calculateBirthDate(2.5)
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Cattle", name = "Kalbo", breed = "Philippine Native",
                birthDate = kalboBirthDate, birthWeight = 35.0, currentWeight = 350.0,
                locationCity = "San Fernando", locationMunicipal = "Pampanga",
                pictureUri = getDrawableUri("listing_2")
            ))
            generateWeightHistory(animalId, kalboBirthDate, 35.0, 350.0, forceWeightDrop = true)

            // Listing 3: Sultan
            val sultanBirthDate = calculateBirthDate(3.5)
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Cattle", name = "Sultan", breed = "Brahman",
                birthDate = sultanBirthDate, birthWeight = 42.0, currentWeight = 470.0,
                locationCity = "General Santos City", locationMunicipal = "SOCCSKSARGEN",
                pictureUri = getDrawableUri("listing_3")
            ))
            generateWeightHistory(animalId, sultanBirthDate, 42.0, 470.0)

            // Listing 4: Pogi
            val pogiBirthDate = calculateBirthDate(1.8)
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Cattle", name = "Pogi", breed = "Philippine Native",
                birthDate = pogiBirthDate, birthWeight = 30.0, currentWeight = 290.0,
                locationCity = "Tarlac City", locationMunicipal = "Central Luzon",
                pictureUri = getDrawableUri("listing_4")
            ))
            generateWeightHistory(animalId, pogiBirthDate, 30.0, 290.0)

            // Listing 5: Bruno
            val brunoBirthDate = calculateBirthDate(4.0)
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Cattle", name = "Bruno", breed = "Ongole–Native Cross",
                birthDate = brunoBirthDate, birthWeight = 40.0, currentWeight = 450.0,
                locationCity = "Davao City", locationMunicipal = "Davao Region",
                pictureUri = getDrawableUri("listing_5")
            ))
            generateWeightHistory(animalId, brunoBirthDate, 40.0, 450.0)

            // Listing 6: Snow (Sheep)
            val snowBirthDate = calculateBirthDate(1.5)
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Sheep", name = "Snow", breed = "Philippine Sheep (Katjang)",
                birthDate = snowBirthDate, birthWeight = 4.0, currentWeight = 38.0,
                locationCity = "La Trinidad", locationMunicipal = "Benguet",
                pictureUri = getDrawableUri("listing_6")
            ))
            generateWeightHistory(animalId, snowBirthDate, 4.0, 38.0)

            // Listing 7: Nene (Sheep)
            val neneBirthDate = calculateBirthDate(2.0)
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Sheep", name = "Nene", breed = "Philippine Native Sheep",
                birthDate = neneBirthDate, birthWeight = 3.5, currentWeight = 30.0,
                locationCity = "Malaybalay", locationMunicipal = "Bukidnon",
                pictureUri = getDrawableUri("listing_7")
            ))
            generateWeightHistory(animalId, neneBirthDate, 3.5, 30.0, forceWeightDrop = true)

            // Listing 8: Tisoy (Goat)
            val tisoyBirthDate = calculateBirthDate(2.5)
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Goat", name = "Tisoy", breed = "Anglo-Nubian Cross",
                birthDate = tisoyBirthDate, birthWeight = 4.0, currentWeight = 42.0,
                locationCity = "Lucena City", locationMunicipal = "CALABARZON",
                pictureUri = getDrawableUri("listing_8")
            ))
            generateWeightHistory(animalId, tisoyBirthDate, 4.0, 42.0)

            // Listing 9: Kikay (Goat)
            val kikayBirthDate = calculateBirthDate(0.5)
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Goat", name = "Kikay", breed = "Philippine Native Goat",
                birthDate = kikayBirthDate, birthWeight = 2.0, currentWeight = 18.0,
                locationCity = "Tagbilaran City", locationMunicipal = "Bohol",
                pictureUri = getDrawableUri("listing_9")
            ))
            generateWeightHistory(animalId, kikayBirthDate, 2.0, 18.0)

            // Listing 10: Blanca (Goat)
            val blancaBirthDate = calculateBirthDate(3.0)
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Goat", name = "Blanca", breed = "Saanen Cross",
                birthDate = blancaBirthDate, birthWeight = 3.8, currentWeight = 40.0,
                locationCity = "Baybay City", locationMunicipal = "Leyte",
                pictureUri = getDrawableUri("listing_10")
            ))
            generateWeightHistory(animalId, blancaBirthDate, 3.8, 40.0)

            // Listing 11: Pinky (Pig)
            val pinkyBirthDate = calculateBirthDate(0.58) // ~7 months
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Pig", name = "Pinky", breed = "Philippine Native–Landrace Cross",
                birthDate = pinkyBirthDate, birthWeight = 1.5, currentWeight = 95.0,
                locationCity = "Cebu City", locationMunicipal = "Cebu",
                pictureUri = getDrawableUri("listing_11")
            ))
            generateWeightHistory(animalId, pinkyBirthDate, 1.5, 95.0)

            // Listing 12: Linda (Pig)
            val lindaBirthDate = calculateBirthDate(2.8)
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Pig", name = "Linda", breed = "Large White–Duroc Cross",
                birthDate = lindaBirthDate, birthWeight = 1.8, currentWeight = 160.0,
                locationCity = "Cagayan de Oro", locationMunicipal = "Misamis Oriental",
                pictureUri = getDrawableUri("listing_12")
            ))
            generateWeightHistory(animalId, lindaBirthDate, 1.8, 160.0)

            // Listing 13: Apollo (Horse)
            val apolloBirthDate = calculateBirthDate(5.0)
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Horse", name = "Apollo", breed = "Philippine Thoroughbred",
                birthDate = apolloBirthDate, birthWeight = 50.0, currentWeight = 490.0,
                locationCity = "Taguig City", locationMunicipal = "Metro Manila",
                pictureUri = getDrawableUri("listing_13")
            ))
            generateWeightHistory(animalId, apolloBirthDate, 50.0, 490.0)

            // Listing 14: Kulas (Horse)
            val kulasBirthDate = calculateBirthDate(6.0)
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Horse", name = "Kulas", breed = "Philippine Native Horse",
                birthDate = kulasBirthDate, birthWeight = 45.0, currentWeight = 310.0,
                locationCity = "Vigan City", locationMunicipal = "Ilocos Sur",
                pictureUri = getDrawableUri("listing_14")
            ))
            generateWeightHistory(animalId, kulasBirthDate, 45.0, 310.0)

            // Listing 15: Baste (Buffalo)
            val basteBirthDate = calculateBirthDate(4.0)
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Buffalo", name = "Baste", breed = "Philippine Carabao",
                birthDate = basteBirthDate, birthWeight = 40.0, currentWeight = 460.0,
                locationCity = "Roxas City", locationMunicipal = "Capiz",
                pictureUri = getDrawableUri("listing_15")
            ))
            generateWeightHistory(animalId, basteBirthDate, 40.0, 460.0)

            // Listing 16: Milky (Buffalo)
            val milkyBirthDate = calculateBirthDate(5.0)
            animalId = animalDao.insertAnimal(Animal(
                animalType = "Buffalo", name = "Milky", breed = "Murrah–Carabao Cross",
                birthDate = milkyBirthDate, birthWeight = 45.0, currentWeight = 520.0,
                locationCity = "Nueva Ecija City", locationMunicipal = "Central Luzon",
                pictureUri = getDrawableUri("listing_16")
            ))
            generateWeightHistory(animalId, milkyBirthDate, 45.0, 520.0)
        }

        private fun calculateBirthDate(ageInYears: Double): Long {
            val calendar = Calendar.getInstance()
            val monthsToSubtract = (ageInYears * 12.0).toInt()
            calendar.add(Calendar.MONTH, -monthsToSubtract)
            return calendar.timeInMillis
        }
    }
}