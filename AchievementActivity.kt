package com.example.atomclicker

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class AchievementsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private var currentPoints = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        currentPoints = intent.getIntExtra("points", 0)
        prefs = getSharedPreferences("AtomClickerPrefs", MODE_PRIVATE)

        loadAchievements()
        setupListeners()
    }

    private fun loadAchievements() {

        val totalClicks = prefs.getInt("total_clicks", 0)


        val achievement1Claimed = prefs.getBoolean("achievement_1_claimed", false)
        val achievement2Claimed = prefs.getBoolean("achievement_2_claimed", false)


        val completedAchievements = listOf(achievement1Claimed, achievement2Claimed).count { it }
        findViewById<TextView>(R.id.tvAchievementProgress).text = "$completedAchievements/2"

        updateAchievementUI(
            achievementId = 1,
            requiredClicks = 1,
            titleViewId = R.id.tvAchievement1Title,
            descViewId = R.id.tvAchievement1Desc,
            rewardViewId = R.id.tvAchievement1Reward,
            imageViewId = R.id.ivAchievement1,
            buttonId = R.id.btnClaimAchievement1,
            claimed = achievement1Claimed,
            reward = 50
        )

        updateAchievementUI(
            achievementId = 2,
            requiredClicks = 100,
            titleViewId = R.id.tvAchievement2Title,
            descViewId = R.id.tvAchievement2Desc,
            rewardViewId = R.id.tvAchievement2Reward,
            imageViewId = R.id.ivAchievement2,
            buttonId = R.id.btnClaimAchievement2,
            claimed = achievement2Claimed,
            reward = 200
        )
    }

    private fun updateAchievementUI(
        achievementId: Int,
        requiredClicks: Int,
        titleViewId: Int,
        descViewId: Int,
        rewardViewId: Int,
        imageViewId: Int,
        buttonId: Int,
        claimed: Boolean,
        reward: Int
    ) {

        val totalClicks = prefs.getInt("total_clicks", 0)

        val imageView = findViewById<ImageView>(imageViewId)
        val titleView = findViewById<TextView>(titleViewId)
        val descView = findViewById<TextView>(descViewId)
        val rewardView = findViewById<TextView>(rewardViewId)
        val button = findViewById<Button>(buttonId)

        val completed = totalClicks >= requiredClicks

        val rewardText = getString(R.string.reward_format, reward)
        rewardView.text = rewardText

        if (claimed) {

            imageView.setColorFilter(ContextCompat.getColor(this, R.color.gold))
            titleView.setTextColor(ContextCompat.getColor(this, R.color.gold))
            descView.setTextColor(Color.parseColor("#B3B3B3"))
            button.text = getString(R.string.claimed)
            button.isEnabled = false
            button.setBackgroundColor(Color.parseColor("#757575"))
        } else if (completed) {

            imageView.setColorFilter(ContextCompat.getColor(this, R.color.green))
            titleView.setTextColor(ContextCompat.getColor(this, R.color.green))
            descView.setTextColor(Color.parseColor("#B3B3B3"))
            button.text = getString(R.string.claim)
            button.isEnabled = true
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
        } else {

            imageView.setColorFilter(Color.parseColor("#757575"))
            titleView.setTextColor(Color.parseColor("#757575"))
            descView.setTextColor(Color.parseColor("#757575"))
            button.text = getString(R.string.claim)
            button.isEnabled = false
            button.setBackgroundColor(Color.parseColor("#757575"))
        }
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btnClaimAchievement1).setOnClickListener {
            claimAchievement(1, 50)
        }

        findViewById<Button>(R.id.btnClaimAchievement2).setOnClickListener {
            claimAchievement(2, 200)
        }
    }

    private fun claimAchievement(achievementId: Int, reward: Int) {

        currentPoints += reward

        prefs.edit().putBoolean("achievement_${achievementId}_claimed", true).apply()
        prefs.edit().putInt("points", currentPoints).apply()

        val toastMessage = getString(R.string.reward_received, reward)
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()

        loadAchievements()

        val returnIntent = Intent()
        returnIntent.putExtra("updated_points", currentPoints)
        setResult(RESULT_OK, returnIntent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}