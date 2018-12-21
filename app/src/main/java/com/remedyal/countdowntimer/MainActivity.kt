package com.remedyal.countdowntimer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.lang.Long.parseLong
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var countDownTimer: CountDownTimer
    private lateinit var editTextInput: EditText
    private lateinit var textViewCountDown: TextView
    private lateinit var buttonStart: Button
    private lateinit var buttonReset: Button
    private lateinit var buttonSet: Button

    private var startTimeInMillis: Long = 0
    private var timeLeftInMillis: Long = 0
    private var endTime: Long = 0
    private var isTimerRunning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextInput = findViewById(R.id.et_input)
        textViewCountDown = findViewById(R.id.tv_countdown)
        buttonStart = findViewById(R.id.btn_start)
        buttonReset = findViewById(R.id.btn_reset)
        buttonSet = findViewById(R.id.btn_set)

        buttonStart.setOnClickListener(clickListener)
        buttonReset.setOnClickListener(clickListener)
        buttonSet.setOnClickListener(clickListener)
    }

    private val clickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.btn_start -> if (isTimerRunning) pauseTimer() else startTimer()
            R.id.btn_reset -> resetTimer()
            R.id.btn_set -> {
                val input = editTextInput.text.toString()
                if (input.isEmpty()) {
                    Toast.makeText(this, "Field can't be empty", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }
                val millisInput: Long = parseLong(input) * 60000

                if (millisInput == 0L) {
                    Toast.makeText(this, "Please enter a positive number", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }

                setStartTime(millisInput)
                editTextInput.setText("")
            }
        }
    }

    private fun setStartTime(ms: Long) {
        startTimeInMillis = ms
        resetTimer()
    }

    private fun startTimer() {
        endTime = System.currentTimeMillis() + timeLeftInMillis

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 500) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                isTimerRunning = false
                updateInterface()
            }
        }.start()

        isTimerRunning = true
        updateInterface()
    }

    private fun pauseTimer() {
        countDownTimer.cancel()
        isTimerRunning = false
        updateInterface()
    }

    private fun resetTimer() {
        timeLeftInMillis = startTimeInMillis
        updateCountDownText()
        updateInterface()
    }

    private fun updateCountDownText() {
        val hours = (timeLeftInMillis / 1000) / 3600
        val minutes = ((timeLeftInMillis / 1000) % 3600) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        var timeLeftFormatted = ""

        if (hours>0){
            timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }

        textViewCountDown.text = timeLeftFormatted
    }

    private fun updateInterface() {
        if (isTimerRunning) {
            editTextInput.visibility = View.INVISIBLE
            buttonSet.visibility = View.INVISIBLE
            buttonReset.visibility = View.INVISIBLE
            buttonStart.text = "pause"
        } else {
            editTextInput.visibility = View.VISIBLE
            buttonSet.visibility = View.VISIBLE
            buttonStart.text = "start"

            if (timeLeftInMillis < 1000) {
                buttonStart.visibility = View.INVISIBLE
            } else {
                buttonStart.visibility = View.VISIBLE
            }

            if (timeLeftInMillis < startTimeInMillis) {
                buttonReset.visibility = View.VISIBLE
            } else {
                buttonReset.visibility = View.INVISIBLE
            }
        }
    }

    override fun onStop() {
        super.onStop()
        var prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        var editor = prefs.edit()
        editor.putLong("startTimeInMillis",startTimeInMillis)
        editor.putLong("timeLeftInMillis", timeLeftInMillis)
        editor.putBoolean("isTimerRunning", isTimerRunning)
        editor.putLong("endTime", endTime)
        editor.apply()

        if (::countDownTimer.isInitialized && countDownTimer != null) {
            countDownTimer.cancel()
        }
    }

    override fun onStart() {
        super.onStart()
        var prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        startTimeInMillis = prefs.getLong("startTimeInMillis", 600000)
        timeLeftInMillis = prefs.getLong("timeLeftInMillis", startTimeInMillis)
        isTimerRunning = prefs.getBoolean("isTimerRunning", false)

        updateCountDownText()
        updateInterface()

        if (isTimerRunning) {
            endTime = prefs.getLong("endTime", 0)
            timeLeftInMillis = endTime - System.currentTimeMillis()

            if (timeLeftInMillis < 0) {
                timeLeftInMillis = 0
                isTimerRunning = false
                updateCountDownText()
                updateInterface()
            } else {
                startTimer()
            }
        }
    }
}
