package com.alptugkafkasli.foodfusion

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.alptugkafkasli.foodfusion.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.firestore.FirebaseFirestore
import firebase.com.protolitewrapper.BuildConfig
import java.util.Locale




class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Firebase Firestore'dan mevcut ve en son sürümü kontrol et
        checkAppVersion()
        val currentLanguage = SharedPreferencesManager.getString(applicationContext, "language", "tr")
        updateResources(this@MainActivity, currentLanguage)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        MobileAds.initialize(this@MainActivity)
        val adView = binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        navigateToFragment(HomeFragment())


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.homefoodfusion -> {
                navigateToFragment(HomeFragment())
                true
            }
            R.id.breakfast -> {
                navigateToFragment(BreakfastFragment())
                true
            }

            R.id.main_course -> {
                navigateToFragment(MaincourseFragment())
                true
            }

            R.id.appetizer -> {
                navigateToFragment(AppetizerFragment())
                true
            }

            R.id.soup -> {
                navigateToFragment(SoupFragment())
                true
            }

            R.id.salad -> {
                navigateToFragment(SaladFragment())
                true
            }

            R.id.dessert -> {
                navigateToFragment(DessertFragment())
                true
            }

            R.id.mezze -> {
                navigateToFragment(MezzeFragment())
                true
            }

            R.id.language_tr -> {
                changeLanguage("tr")
                true
            }

            R.id.language_en -> {
                changeLanguage("en")
                true
            }


            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun changeLanguage(language: String) {
        updateResources(this, language)
        SharedPreferencesManager.putString(applicationContext, "language", language)
        val intent = intent
        finish()
        startActivity(intent)
    }

    private fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun updateResources(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
    private fun checkAppVersion() {
        val currentVersionCode = com.alptugkafkasli.foodfusion.BuildConfig.VERSION_CODE
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("Versions").document("Android")

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val latestVersionCode = document.getLong("versionCode") ?: 0

                    if (latestVersionCode > currentVersionCode) {
                        showUpdateDialog()
                    }
                } else {
                    // Firestore'dan doküman alınamazsa bir hata mesajı gösterebilirsiniz
                }
            }
            .addOnFailureListener { exception ->
                // Firestore'a erişim hatası olduğunda burada hata mesajı gösterebilirsiniz
                
            }
    }
    private fun showUpdateDialog() {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Uygulama Güncelle")
            .setMessage("Yeni bir sürüm mevcut, uygulamayı güncellemek için Google Play Store'a yönlendirileceksiniz.")
            .setPositiveButton("Güncelle") { dialog, which ->
                redirectToPlayStore()
            }
            .setCancelable(false)
            .show()
    }
    private fun redirectToPlayStore() {
        val appPackageName = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (e: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }




}
