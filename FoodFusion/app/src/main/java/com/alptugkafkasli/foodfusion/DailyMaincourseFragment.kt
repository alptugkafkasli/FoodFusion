import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alptugkafkasli.foodfusion.databinding.FragmentDailyMaincourseBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class DailyMaincourseFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private var lastUpdateTime: Long = 0
    private lateinit var db: FirebaseFirestore

    private var _binding: FragmentDailyMaincourseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDailyMaincourseBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("MyMaincourse", Context.MODE_PRIVATE)
        db = Firebase.firestore
        checkAndUpdateData()
        return binding.root
    }

    private fun checkAndUpdateData() {
        val currentTime = Calendar.getInstance().timeInMillis
        val lastUpdatedTime = sharedPreferences.getLong(LAST_UPDATE_TIME_KEY_MAINCOURSE, 0)

        if (currentTime - lastUpdatedTime >= 24 * 60 * 60 * 1000) {
            // Yeni veriyi al
            fetchDataFromFirestore()
        } else {
            // Saklanan veriyi kullan
            val name = sharedPreferences.getString(NAME_KEY_MAINCOURSE, "")
            val description = sharedPreferences.getString(DESCRIPTION_KEY_MAINCOURSE, "")
            val imageUrl = sharedPreferences.getString(IMAGE_URL_KEY_MAINCOURSE, "")
            val recipes = sharedPreferences.getString(RECIPES_KEY_MAINCOURSE, "")
            val ingredients = sharedPreferences.getString(INGREDIENTS_KEY_MAINCOURSE, "")

            // Arayüzü güncelle
            updateUI(name, description, imageUrl, recipes, ingredients)
        }
    }

    private fun fetchDataFromFirestore() {
        db.collection("Maincourse").get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d(TAG_MAINCOURSE, "Belge bulunamadı.")
                    return@addOnSuccessListener
                }

                val belgeListesi = documents.toMutableList()
                val rastgeleBelge = belgeListesi.random()

                val name = rastgeleBelge.getString("Name")
                val description = rastgeleBelge.getString("Description")
                val imageUrl = rastgeleBelge.getString("ImageUrl")
                val recipes = rastgeleBelge.get("Recipes") as? List<String>
                val ingredients = rastgeleBelge.get("Ingredients") as? List<String>

                // SharedPreferences'a veriyi kaydet
                with(sharedPreferences.edit()) {
                    putString(NAME_KEY_MAINCOURSE, name)
                    putString(DESCRIPTION_KEY_MAINCOURSE, description)
                    putString(IMAGE_URL_KEY_MAINCOURSE, imageUrl)
                    putString(RECIPES_KEY_MAINCOURSE, recipes?.joinToString("\n\n"))
                    putString(INGREDIENTS_KEY_MAINCOURSE, ingredients?.joinToString("\n\n"))
                    putLong(LAST_UPDATE_TIME_KEY_MAINCOURSE, Calendar.getInstance().timeInMillis)
                    apply()
                }

                // Arayüzü güncelle
                updateUI(name, description, imageUrl, recipes?.joinToString("\n\n"), ingredients?.joinToString("\n\n"))
            }
            .addOnFailureListener { exception ->
                Log.w(TAG_MAINCOURSE, "Belgeleri alma işlemi başarısız oldu.", exception)
            }
    }

    private fun updateUI(name: String?, description: String?, imageUrl: String?, recipes: String?, ingredients: String?) {
        binding.nameMaincourse.text = name
        binding.descriptionMaincourse.text = description

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(binding.imageMaincourse)
        } else {
            Log.e(TAG_MAINCOURSE, "ImageUrl boş veya null")
        }

        binding.recipesMaincourse.text = recipes ?: "Tarif bulunamadı veya boş."
        binding.ingredientsMaincourse.text = ingredients ?: "Malzeme bulunamadı veya boş."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG_MAINCOURSE = "DailyMaincourseFragment"
        private const val LAST_UPDATE_TIME_KEY_MAINCOURSE = "son_guncelleme_zamani"
        private const val NAME_KEY_MAINCOURSE = "isim"
        private const val DESCRIPTION_KEY_MAINCOURSE = "aciklama"
        private const val IMAGE_URL_KEY_MAINCOURSE = "resim_url"
        private const val RECIPES_KEY_MAINCOURSE = "tarifler"
        private const val INGREDIENTS_KEY_MAINCOURSE = "malzemeler"
    }
}
