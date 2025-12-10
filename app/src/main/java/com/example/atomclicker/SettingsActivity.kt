package com.example.atomclicker

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var spinnerLanguage: Spinner
    private lateinit var btnResetProgress: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setAppLanguage()

        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("AtomClickerPrefs", MODE_PRIVATE)

        initViews()
        loadSettings()
        setupListeners()
    }

    private fun setAppLanguage() {
        val prefs = getSharedPreferences("AtomClickerPrefs", MODE_PRIVATE)
        val languageCode = prefs.getString("language_code", "en") ?: "en"

        val locale = when (languageCode) {
            "ru" -> Locale("ru", "RU")
            else -> Locale.ENGLISH
        }

        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun initViews() {
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        btnResetProgress = findViewById(R.id.btnResetProgress)

        val languages = resources.getStringArray(R.array.languages)
        if (languages.isNotEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerLanguage.adapter = adapter
        } else {
            val defaultLanguages = arrayOf("English", "Русский")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defaultLanguages)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerLanguage.adapter = adapter
        }
    }

    private fun loadSettings() {
        val savedLanguageCode = prefs.getString("language_code", "en") ?: "en"

        val languageCodes = resources.getStringArray(R.array.language_codes)

        var position = 0
        for (i in languageCodes.indices) {
            if (languageCodes[i] == savedLanguageCode) {
                position = i
                break
            }
        }

        spinnerLanguage.setSelection(position)
    }

    private fun setupListeners() {
        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {

                val languageCodes = resources.getStringArray(R.array.language_codes)
                val selectedLanguageCode = languageCodes[position]

                val languages = resources.getStringArray(R.array.languages)
                val selectedLanguageName = languages[position]

                val currentLanguageCode = prefs.getString("language_code", "en")
                if (currentLanguageCode != selectedLanguageCode) {
                    prefs.edit().putString("language_code", selectedLanguageCode).apply()
                    showLanguageChangeDialog(selectedLanguageName)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnResetProgress.setOnClickListener {
            showResetConfirmationDialog()
        }
    }

    private fun showLanguageChangeDialog(languageName: String) {
        val dialogTitle = getString(R.string.language_change)
        val dialogMessage = getString(R.string.language_changed, languageName)
        val okButtonText = getString(R.string.ok)

        AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .setPositiveButton(okButtonText) { dialog, _ ->
                dialog.dismiss()
                restartApp()
            }
            .setCancelable(false)
            .show()
    }

    private fun showResetConfirmationDialog() {
        val dialogTitle = getString(R.string.reset_title)
        val dialogMessage = getString(R.string.reset_message)
        val resetButtonText = getString(R.string.reset_confirm)
        val cancelButtonText = getString(R.string.cancel)

        AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .setPositiveButton(resetButtonText) { dialog, _ ->
                resetProgress()
                dialog.dismiss()
            }
            .setNegativeButton(cancelButtonText) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun resetProgress() {
        prefs.edit().clear().apply()

        prefs.edit().apply {
            putString("language_code", "en")
            putInt("points", 0)
            putInt("click_upgrade_level", 0)
            putInt("autoclicker_level", 0)
            putInt("multiplier_level", 0)
            apply()
        }

        val toastMessage = getString(R.string.reset_complete)
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}