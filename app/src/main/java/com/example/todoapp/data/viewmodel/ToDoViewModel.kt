package com.example.todoapp.data.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.ToDoDatabase
import com.example.todoapp.data.models.ToDoData
import com.example.todoapp.data.repository.ToDoRepository
import com.example.todoapp.receiver.AlarmReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ToDoViewModel(application: Application) : AndroidViewModel(application) {

    private val toDoDao = ToDoDatabase.getDatabase(
        application
    ).toDoDao()
    private val repository: ToDoRepository = ToDoRepository(toDoDao)

    val getAllData: LiveData<List<ToDoData>> = repository.getAllData
    val sortByHighPriority: LiveData<List<ToDoData>> = repository.sortByHighPriority
    val sortByLowPriority: LiveData<List<ToDoData>> = repository.sortByLowPriority

    // Insert Data and Schedule Alarm
    fun insertData(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertData(toDoData)
            // Schedule the alarm for the task's due time
            scheduleAlarm(getApplication(), toDoData.id, toDoData.dueTimeInMillis)
        }
    }

    // Update Data and Reschedule Alarm
    fun updateData(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateData(toDoData)
            // Reschedule the alarm if the task time is updated
            scheduleAlarm(getApplication(), toDoData.id, toDoData.dueTimeInMillis)
        }
    }

    // Delete a task and cancel the associated alarm
    fun deleteItem(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteItem(toDoData)
            // Cancel the alarm if the task is deleted
            cancelAlarm(getApplication(), toDoData.id)
        }
    }

    // Delete all tasks
    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
            // Optionally, you could cancel all alarms here
        }
    }

    // Search database
    fun searchDatabase(searchQuery: String): LiveData<List<ToDoData>> {
        return repository.searchDatabase(searchQuery)
    }

    // Schedule an alarm for a task
    // Schedule an exact alarm
    private fun scheduleAlarm(context: Context, taskId: Int, timeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("notificationId", taskId)
            putExtra("message", "Your task reminder") // Customize the message
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use setExactAndAllowWhileIdle for exact alarms even in Doze mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }

    // Cancel an existing alarm
    private fun cancelAlarm(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
