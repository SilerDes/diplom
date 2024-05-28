package com.kazbekov.invent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {
    private val configViewModel: ConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            configViewModel.code = savedInstanceState.getInt(KEY_CONFIG_CODE)
            configViewModel.statusCode = savedInstanceState.getInt(KEY_CONFIG_STATUS)
        }
        //Отключаем ночную тему системно, до разработки ресурсов под ночную тему
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)

        configViewModel.code?.let { code ->
            outState.putInt(KEY_CONFIG_CODE, code)
            outState.putInt(KEY_CONFIG_STATUS, configViewModel.statusCode)
        }

    }

    companion object {
        private const val KEY_CONFIG_CODE = "config_code"
        private const val KEY_CONFIG_STATUS = "config_status"
    }
}