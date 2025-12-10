package com.example.atomclicker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow

class UpgradesActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var tvBalance: TextView
    private lateinit var tvClickUpgradeLevel: TextView
    private lateinit var tvClickUpgradeCost: TextView
    private lateinit var btnBuyClickUpgrade: Button
    private lateinit var tvAutoclickerLevel: TextView
    private lateinit var tvAutoclickerCost: TextView
    private lateinit var btnBuyAutoclicker: Button
    private lateinit var tvMultiplierLevel: TextView
    private lateinit var tvMultiplierCost: TextView
    private lateinit var btnBuyMultiplier: Button

    private var points = 0
    private var clickUpgradeLevel = 0
    private var autoclickerLevel = 0
    private var multiplierLevel = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upgrades)

        prefs = getSharedPreferences("AtomClickerPrefs", MODE_PRIVATE)

        initViews()
        loadData()
        updateUI()
        setupListeners()
    }

    private fun initViews() {
        tvBalance = findViewById(R.id.tvBalance)
        tvClickUpgradeLevel = findViewById(R.id.tvClickUpgradeLevel)
        tvClickUpgradeCost = findViewById(R.id.tvClickUpgradeCost)
        btnBuyClickUpgrade = findViewById(R.id.btnBuyClickUpgrade)
        tvAutoclickerLevel = findViewById(R.id.tvAutoclickerLevel)
        tvAutoclickerCost = findViewById(R.id.tvAutoclickerCost)
        btnBuyAutoclicker = findViewById(R.id.btnBuyAutoclicker)
        tvMultiplierLevel = findViewById(R.id.tvMultiplierLevel)
        tvMultiplierCost = findViewById(R.id.tvMultiplierCost)
        btnBuyMultiplier = findViewById(R.id.btnBuyMultiplier)
    }

    private fun loadData() {
        points = intent.getIntExtra("points", 0)
        clickUpgradeLevel = prefs.getInt("click_upgrade_level", 0)
        autoclickerLevel = prefs.getInt("autoclicker_level", 0)
        multiplierLevel = prefs.getInt("multiplier_level", 0)
    }

    private fun updateUI() {
        tvBalance.text = points.toString()

        tvClickUpgradeLevel.text = "Current level: $clickUpgradeLevel"
        val clickUpgradeCost = calculateCost(clickUpgradeLevel, 100)
        tvClickUpgradeCost.text = "Cost: $clickUpgradeCost points"
        btnBuyClickUpgrade.isEnabled = points >= clickUpgradeCost

        tvAutoclickerLevel.text = "Current level: $autoclickerLevel"
        val autoclickerCost = calculateCost(autoclickerLevel, 500)
        tvAutoclickerCost.text = "Cost: $autoclickerCost points"
        btnBuyAutoclicker.isEnabled = points >= autoclickerCost

        tvMultiplierLevel.text = "Current level: $multiplierLevel"
        val multiplierCost = calculateCost(multiplierLevel, 1000)
        tvMultiplierCost.text = "Cost: $multiplierCost points"
        btnBuyMultiplier.isEnabled = points >= multiplierCost
    }

    private fun calculateCost(level: Int, baseCost: Int): Int {
        return (baseCost * 1.5.pow(level)).toInt()
    }

    private fun setupListeners() {
        btnBuyClickUpgrade.setOnClickListener {
            val cost = calculateCost(clickUpgradeLevel, 100)
            if (points >= cost) {
                points -= cost
                clickUpgradeLevel++

                prefs.edit().apply {
                    putInt("click_upgrade_level", clickUpgradeLevel)
                    putInt("points", points)
                    apply()
                }

                updateUI()
                showToast(R.string.upgrade_purchased)
            } else {
                showToast(R.string.not_enough_points)
            }
        }

        btnBuyAutoclicker.setOnClickListener {
            val cost = calculateCost(autoclickerLevel, 500)
            if (points >= cost) {
                points -= cost
                autoclickerLevel++

                prefs.edit().apply {
                    putInt("autoclicker_level", autoclickerLevel)
                    putInt("points", points)
                    apply()
                }

                updateUI()
                showToast(R.string.autoclicker_purchased)
            } else {
                showToast(R.string.not_enough_points)
            }
        }

        btnBuyMultiplier.setOnClickListener {
            val cost = calculateCost(multiplierLevel, 1000)
            if (points >= cost) {
                points -= cost
                multiplierLevel++

                prefs.edit().apply {
                    putInt("multiplier_level", multiplierLevel)
                    putInt("points", points)
                    apply()
                }

                updateUI()
                showToast(R.string.multiplier_purchased)
            } else {
                showToast(R.string.not_enough_points)
            }
        }
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(this, getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        val resultIntent = Intent()
        resultIntent.putExtra("updated_points", points)
        setResult(RESULT_OK, resultIntent)
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}