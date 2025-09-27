import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.tagriculture.data.database.AppDatabase
import com.example.tagriculture.data.database.Notification

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {
    private val notificationDao = AppDatabase.getDatabase(application).notificationDao()
    val allNotifications: LiveData<List<Notification>> = notificationDao.getAllNotifications()
}