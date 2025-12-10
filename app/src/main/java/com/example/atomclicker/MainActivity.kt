package com.example.atomclicker

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    private val electronAnimators = mutableListOf<ElectronAnimatorData>()
    private var points = 0
    private var pointsPerClick = 1
    private var pointsPerSecond = 0
    private var clickMultiplier = 1.0f
    private var totalClicks = 0

    private lateinit var pointsTextView: TextView
    private lateinit var perSecondTextView: TextView
    private lateinit var atomContainer: FrameLayout
    private lateinit var nucleus: ImageView
    private lateinit var clickText: TextView
    private lateinit var clickEffect1: ImageView
    private lateinit var clickEffect2: ImageView
    private lateinit var clickEffect3: ImageView
    private lateinit var btnHome: LinearLayout
    private lateinit var btnUpgrades: LinearLayout
    private lateinit var btnAchievements: LinearLayout
    private lateinit var btnSettings: LinearLayout

    private val handler = Handler(Looper.getMainLooper())
    private val speedUpHandlers = mutableMapOf<ValueAnimator, Handler>()
    private lateinit var prefs: SharedPreferences

    private val updatePointsRunnable = object : Runnable {
        override fun run() {
            if (pointsPerSecond > 0) {
                points += pointsPerSecond
                updatePointsDisplay()
                saveProgress()
            }
            handler.postDelayed(this, 1000)
        }
    }

    private val upgradesResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                points = data.getIntExtra("updated_points", points)
                updatePointsDisplay()
                saveProgress()
            }
        }
    }

    private val achievementsResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                points = data.getIntExtra("updated_points", points)
                updatePointsDisplay()
                saveProgress()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setAppLanguage()

        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("AtomClickerPrefs", MODE_PRIVATE)
        loadProgress()

        setupViews()
        setupNavigation()
        setupClickListeners()

        handler.postDelayed({
            setupOrbits()
        }, 500)

        handler.postDelayed(updatePointsRunnable, 1000)
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

    private fun setupViews() {
        pointsTextView = findViewById(R.id.Points)
        perSecondTextView = findViewById(R.id.PerSecond)
        atomContainer = findViewById(R.id.AtomContainer)
        nucleus = findViewById(R.id.Nucleus)
        clickText = findViewById(R.id.ClickText)
        clickEffect1 = findViewById(R.id.ClickEffect1)
        clickEffect2 = findViewById(R.id.ClickEffect2)
        clickEffect3 = findViewById(R.id.ClickEffect3)

        btnHome = findViewById(R.id.btnHome)
        btnUpgrades = findViewById(R.id.btnUpgrades)
        btnAchievements = findViewById(R.id.btnAchievements)
        btnSettings = findViewById(R.id.btnSettings)

        updatePointsDisplay()
        updateTextsForCurrentLanguage()
    }

    private fun updateTextsForCurrentLanguage() {
        val upgradesTextView = btnUpgrades.getChildAt(1) as? TextView
        val homeTextView = btnHome.getChildAt(1) as? TextView
        val achievementsTextView = btnAchievements.getChildAt(1) as? TextView
        val settingsTextView = btnSettings.getChildAt(1) as? TextView

        upgradesTextView?.text = getString(R.string.upgrades)
        homeTextView?.text = getString(R.string.atom)
        achievementsTextView?.text = getString(R.string.achievements)
        settingsTextView?.text = getString(R.string.settings)
    }

    private fun setupNavigation() {
        highlightTab(btnHome)

        btnUpgrades.setOnClickListener {
            val intent = Intent(this, UpgradesActivity::class.java)
            intent.putExtra("points", points)
            upgradesResultLauncher.launch(intent)
        }

        btnAchievements.setOnClickListener {
            val intent = Intent(this, AchievementsActivity::class.java)
            intent.putExtra("points", points)
            achievementsResultLauncher.launch(intent)
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun highlightTab(tab: LinearLayout) {
        val tabs = listOf(btnHome, btnUpgrades, btnAchievements, btnSettings)

        tabs.forEach { tabView ->
            tabView.background = null
            val textView = tabView.getChildAt(1) as? TextView
            val imageView = tabView.getChildAt(0) as? ImageView

            textView?.setTextColor(ContextCompat.getColor(this, R.color.light_blue))
            imageView?.setColorFilter(ContextCompat.getColor(this, R.color.light_blue))
        }

        tab.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_blue))
        val textView = tab.getChildAt(1) as? TextView
        val imageView = tab.getChildAt(0) as? ImageView

        textView?.setTextColor(ContextCompat.getColor(this, R.color.white))
        imageView?.setColorFilter(ContextCompat.getColor(this, R.color.white))
    }

    private fun setupClickListeners() {
        atomContainer.setOnClickListener {
            onAtomClick()
        }

        atomContainer.setOnLongClickListener {
            onAtomLongClick()
            true
        }
    }

    private fun onAtomClick() {
        totalClicks = prefs.getInt("total_clicks", 0)
        totalClicks++
        prefs.edit().putInt("total_clicks", totalClicks).apply()

        loadUpgrades()

        val pointsToAdd = (pointsPerClick * clickMultiplier).toInt()
        points += pointsToAdd

        updatePointsDisplay()
        animateNucleusClick()
        animateClickText(pointsToAdd)
        animateClickParticles()
        gentlySpeedUpElectrons()
        animateContainerFeedback()

        saveProgress()
    }

    private fun onAtomLongClick() {
        totalClicks = prefs.getInt("total_clicks", 0)
        totalClicks++
        prefs.edit().putInt("total_clicks", totalClicks).apply()

        val bonusPoints = pointsPerClick * 10
        points += bonusPoints
        updatePointsDisplay()
        animateBonusEffect()
        saveProgress()
    }

    private fun loadUpgrades() {
        val clickUpgradeLevel = prefs.getInt("click_upgrade_level", 0)
        val autoclickerLevel = prefs.getInt("autoclicker_level", 0)
        val multiplierLevel = prefs.getInt("multiplier_level", 0)

        val basePointsPerClick = when {
            points >= 10000 -> 50
            points >= 5000 -> 20
            points >= 1000 -> 10
            points >= 500 -> 5
            points >= 100 -> 2
            else -> 1
        }

        val basePointsPerSecond = when {
            points >= 10000 -> 10
            points >= 5000 -> 5
            points >= 1000 -> 1
            else -> 0
        }

        pointsPerClick = ((basePointsPerClick + clickUpgradeLevel) * 1.5.pow(multiplierLevel)).toInt()
        pointsPerSecond = basePointsPerSecond + autoclickerLevel

        clickMultiplier = if (points >= 10000) 1.2f else 1.0f

        runOnUiThread {
            val perSecondText = getString(R.string.points_per_second_format, pointsPerSecond)
            perSecondTextView.text = perSecondText
        }
    }

    private fun animateContainerFeedback() {
        atomContainer.animate()
            .scaleX(1.02f)
            .scaleY(1.02f)
            .setDuration(80)
            .withEndAction {
                atomContainer.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(120)
                    .start()
            }
            .start()
    }

    private fun gentlySpeedUpElectrons() {
        electronAnimators.forEach { data ->
            val animator = data.animator
            speedUpHandlers[animator]?.removeCallbacksAndMessages(null)

            val durationAnimator = ValueAnimator.ofFloat(1f, 0.8f)
            durationAnimator.duration = 200
            durationAnimator.addUpdateListener { valueAnimator ->
                val multiplier = valueAnimator.animatedValue as Float
                animator.duration = (data.originalDuration * multiplier).toLong()
            }
            durationAnimator.start()

            val restoreHandler = Handler(Looper.getMainLooper())
            speedUpHandlers[animator] = restoreHandler

            restoreHandler.postDelayed({
                val restoreAnimator = ValueAnimator.ofFloat(0.8f, 1f)
                restoreAnimator.duration = 300
                restoreAnimator.addUpdateListener { valueAnimator ->
                    val multiplier = valueAnimator.animatedValue as Float
                    animator.duration = (data.originalDuration * multiplier).toLong()
                }
                restoreAnimator.start()
                speedUpHandlers.remove(animator)
            }, 800)
        }
    }

    private fun animateNucleusClick() {
        val scaleUp = ObjectAnimator.ofFloat(nucleus, "scaleX", 1f, 1.3f)
        val scaleUpY = ObjectAnimator.ofFloat(nucleus, "scaleY", 1f, 1.3f)
        val scaleDown = ObjectAnimator.ofFloat(nucleus, "scaleX", 1.3f, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(nucleus, "scaleY", 1.3f, 1f)

        scaleUp.duration = 100
        scaleUpY.duration = 100
        scaleDown.duration = 200
        scaleDownY.duration = 200

        scaleUp.interpolator = OvershootInterpolator(1.5f)
        scaleDown.interpolator = AccelerateDecelerateInterpolator()

        val animatorSet = AnimatorSet()
        animatorSet.play(scaleUp).with(scaleUpY)
        animatorSet.play(scaleDown).with(scaleDownY).after(scaleUp)
        animatorSet.start()
    }

    private fun animateClickText(pointsToAdd: Int) {
        runOnUiThread {
            val clickTextFormatted = getString(R.string.click_text_format, pointsToAdd)
            clickText.text = clickTextFormatted
            clickText.alpha = 1f
            clickText.translationY = 0f
            clickText.scaleX = 1f
            clickText.scaleY = 1f

            val fadeOut = ObjectAnimator.ofFloat(clickText, "alpha", 1f, 0f)
            val moveUp = ObjectAnimator.ofFloat(clickText, "translationY", 0f, -80f)
            val scaleUp = ObjectAnimator.ofFloat(clickText, "scaleX", 1f, 1.2f)
            val scaleUpY = ObjectAnimator.ofFloat(clickText, "scaleY", 1f, 1.2f)

            fadeOut.duration = 600
            moveUp.duration = 600
            scaleUp.duration = 300
            scaleUpY.duration = 300

            val scaleDown = ObjectAnimator.ofFloat(clickText, "scaleX", 1.2f, 1f)
            val scaleDownY = ObjectAnimator.ofFloat(clickText, "scaleY", 1.2f, 1f)
            scaleDown.duration = 300
            scaleDownY.duration = 300

            val animatorSet = AnimatorSet()
            animatorSet.play(scaleUp).with(scaleUpY)
            animatorSet.play(scaleDown).with(scaleDownY).after(scaleUp)
            animatorSet.play(fadeOut).with(moveUp).after(scaleUp)
            animatorSet.start()
        }
    }

    private fun animateClickParticles() {
        val particles = listOf(clickEffect1, clickEffect2, clickEffect3)
        val angles = listOf(0f, 120f, 240f)

        particles.forEachIndexed { index, particle ->
            particle.alpha = 1f
            particle.scaleX = 1f
            particle.scaleY = 1f
            particle.translationX = 0f
            particle.translationY = 0f

            val angle = angles[index]
            val distance = 60f

            val radians = Math.toRadians(angle.toDouble())
            val targetX = distance * cos(radians).toFloat()
            val targetY = distance * sin(radians).toFloat()

            val moveX = ObjectAnimator.ofFloat(particle, "translationX", 0f, targetX)
            val moveY = ObjectAnimator.ofFloat(particle, "translationY", 0f, targetY)
            val fadeOut = ObjectAnimator.ofFloat(particle, "alpha", 1f, 0f)
            val scale = ObjectAnimator.ofFloat(particle, "scaleX", 1f, 0.8f)
            val scaleY = ObjectAnimator.ofFloat(particle, "scaleY", 1f, 0.8f)

            moveX.duration = 400
            moveY.duration = 400
            fadeOut.duration = 400
            scale.duration = 400
            scaleY.duration = 400

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(moveX, moveY, fadeOut, scale, scaleY)
            animatorSet.startDelay = index * 80L
            animatorSet.start()
        }
    }

    private fun animateBonusEffect() {
        val orbits = listOfNotNull(
            findViewById<ImageView>(R.id.Orbit1),
            findViewById<ImageView>(R.id.Orbit2),
            findViewById<ImageView>(R.id.Orbit3)
        )

        orbits.forEach { orbit ->
            orbit.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .alpha(0.6f)
                .setDuration(250)
                .withEndAction {
                    orbit.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(0.4f)
                        .setDuration(250)
                        .start()
                }
                .start()
        }

        runOnUiThread {
            clickText.text = getString(R.string.bonus_text)
            clickText.setTextColor(ContextCompat.getColor(this, R.color.orange))
            clickText.alpha = 1f
            clickText.scaleX = 0.8f
            clickText.scaleY = 0.8f

            val scaleUp = ObjectAnimator.ofFloat(clickText, "scaleX", 0.8f, 1.3f)
            val scaleUpY = ObjectAnimator.ofFloat(clickText, "scaleY", 0.8f, 1.3f)
            val fadeOut = ObjectAnimator.ofFloat(clickText, "alpha", 1f, 0f)

            scaleUp.duration = 400
            scaleUpY.duration = 400
            fadeOut.duration = 400

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(scaleUp, scaleUpY, fadeOut)
            animatorSet.start()

            Handler(Looper.getMainLooper()).postDelayed({
                clickText.setTextColor(ContextCompat.getColor(this, R.color.green))
            }, 500)
        }

        val bonusSpeedUpHandler = Handler(Looper.getMainLooper())
        electronAnimators.forEach { data ->
            val animator = data.animator
            val originalDuration = animator.duration
            animator.duration = (originalDuration * 0.7f).toLong()

            bonusSpeedUpHandler.postDelayed({
                animator.duration = originalDuration
            }, 1500)
        }
    }

    private fun updatePointsDisplay() {
        runOnUiThread {
            val formattedPoints = when {
                points >= 1_000_000 -> String.format("%.1fM", points / 1_000_000.0)
                points >= 1_000 -> String.format("%.1fK", points / 1_000.0)
                else -> points.toString()
            }

            pointsTextView.text = formattedPoints
        }
    }

    private fun setupOrbits() {
        val container = findViewById<FrameLayout>(R.id.AtomContainer)

        if (container.width == 0 || container.height == 0) {
            handler.postDelayed({
                setupOrbits()
            }, 100)
            return
        }

        val centerX = container.width / 2f
        val centerY = container.height / 2f

        val orbits = listOf(
            OrbitData(50f, 50f, 0f, 2, 7000L, listOf(R.id.Electron1, R.id.Electron1_2)),
            OrbitData(90f, 60f, 45f, 2, 10000L, listOf(R.id.Electron2, R.id.Electron2_2)),
            OrbitData(120f, 80f, -45f, 1, 13000L, listOf(R.id.Electron3))
        )

        orbits.forEach { orbit ->
            val angleRad = Math.toRadians(orbit.angleDeg.toDouble()).toFloat()

            orbit.electronIds.forEachIndexed { index, electronId ->
                val electron = findViewById<ImageView>(electronId) ?: return@forEachIndexed
                val phaseOffset = (2f * PI.toFloat() / orbit.electronCount) * index

                val animator = createEllipticalOrbitAnimator(
                    electron,
                    centerX,
                    centerY,
                    dpToPx(orbit.radiusX),
                    dpToPx(orbit.radiusY),
                    angleRad,
                    phaseOffset,
                    orbit.duration
                )

                electronAnimators.add(ElectronAnimatorData(animator, orbit.duration, electron))
            }
        }
    }

    private fun createEllipticalOrbitAnimator(
        view: View,
        centerX: Float,
        centerY: Float,
        radiusX: Float,
        radiusY: Float,
        orbitAngle: Float,
        phaseOffset: Float,
        duration: Long
    ): ValueAnimator {
        val animator = ValueAnimator.ofFloat(0f, 2 * PI.toFloat())
        animator.duration = duration
        animator.repeatCount = ValueAnimator.INFINITE
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { valueAnimator ->
            val angle = valueAnimator.animatedValue as Float + phaseOffset

            val x = radiusX * cos(angle)
            val y = radiusY * sin(angle)

            val cosA = cos(orbitAngle)
            val sinA = sin(orbitAngle)

            val finalX = centerX + (x * cosA - y * sinA)
            val finalY = centerY + (x * sinA + y * cosA)

            view.x = finalX - view.width / 2f
            view.y = finalY - view.height / 2f
        }

        animator.start()
        return animator
    }

    private fun saveProgress() {
        prefs.edit()
            .putInt("points", points)
            .apply()
    }

    private fun loadProgress() {
        points = prefs.getInt("points", 0)
        totalClicks = prefs.getInt("total_clicks", 0)
    }

    override fun onPause() {
        super.onPause()
        electronAnimators.forEach { it.animator.pause() }
        handler.removeCallbacks(updatePointsRunnable)
        speedUpHandlers.values.forEach { it.removeCallbacksAndMessages(null) }
        speedUpHandlers.clear()
        saveProgress()
    }

    override fun onResume() {
        super.onResume()
        electronAnimators.forEach { it.animator.resume() }
        handler.postDelayed(updatePointsRunnable, 1000)
        loadProgress()
        loadUpgrades()
        updatePointsDisplay()
    }

    override fun onDestroy() {
        super.onDestroy()
        electronAnimators.forEach {
            it.animator.removeAllUpdateListeners()
            it.animator.cancel()
        }
        electronAnimators.clear()

        handler.removeCallbacksAndMessages(null)
        speedUpHandlers.values.forEach { it.removeCallbacksAndMessages(null) }
        speedUpHandlers.clear()

        saveProgress()
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    data class OrbitData(
        val radiusX: Float,
        val radiusY: Float,
        val angleDeg: Float,
        val electronCount: Int,
        val duration: Long,
        val electronIds: List<Int>
    )

    data class ElectronAnimatorData(
        val animator: ValueAnimator,
        val originalDuration: Long,
        val view: View
    )
}