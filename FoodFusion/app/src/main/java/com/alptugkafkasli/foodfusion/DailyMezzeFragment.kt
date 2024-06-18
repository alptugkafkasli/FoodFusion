import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alptugkafkasli.foodfusion.databinding.FragmentDailyMezzeBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class DailyMezzeFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private var lastUpdateTime: Long = 0
    private lateinit var db: FirebaseFirestore

    private var _binding: FragmentDailyMezzeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDailyMezzeBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("MyMezze", Context.MODE_PRIVATE)
        db = Firebase.firestore
        checkAndUpdateData()
        return binding.root
    }

    private fun checkAndUpdateData() {
        val currentTime = Calendar.getInstance().timeInMillis
        val lastUpdatedTime = sharedPreferences.getLong(LAST_UPDATE_TIME_KEY_MEZZE, 0)

        if (currentTime - lastUpdatedTime >= 24 * 60 * 60 * 1000) {
            // Yeni veriyi al
            fetchDataFromFirestore()
        } else {
            // Saklanan veriyi kullan
            val name = sharedPreferences.getString(NAME_KEY_MEZZE, "")
            val description = sharedPreferences.getString(DESCRIPTION_KEY_MEZZE, "")
            val imageUrl = sharedPreferences.getString(IMAGE_URL_KEY_MEZZE, "")
            val recipes = sharedPreferences.getString(RECIPES_KEY_MEZZE, "")
            val ingredients = sharedPreferences.getString(INGREDIENTS_KEY_MEZZE, "")

            // Arayüzü güncelle
            updateUI(name, description, imageUrl, recipes, ingredients)
        }
    }

    private fun fetchDataFromFirestore() {
        db.collection("Mezze").get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d(TAG_MEZZE, "Belge bulunamadı.")
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
                    putString(NAME_KEY_MEZZE, name)
                    putString(DESCRIPTION_KEY_MEZZE, description)
                    putString(IMAGE_URL_KEY_MEZZE, imageUrl)
                    putString(RECIPES_KEY_MEZZE, recipes?.joinToString("\n\n"))
                    putString(INGREDIENTS_KEY_MEZZE, ingredients?.joinToString("\n\n"))
                    putLong(LAST_UPDATE_TIME_KEY_MEZZE, Calendar.getInstance().timeInMillis)
                    apply()
                }

                // Arayüzü güncelle
                updateUI(name, description, imageUrl, recipes?.joinToString("\n\n"), ingredients?.joinToString("\n\n"))
            }
            .addOnFailureListener { exception ->
                Log.w(TAG_MEZZE, "Belgeleri alma işlemi başarısız oldu.", exception)
            }
    }

    private fun updateUI(name: String?, description: String?, imageUrl: String?, recipes: String?, ingredients: String?) {
        binding.nameMezze.text = name
        binding.descriptionMezze.text = description

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(binding.imageMezze)
        } else {
            Log.e(TAG_MEZZE, "ImageUrl boş veya null")
        }

        binding.recipesMezze.text = recipes ?: "Tarif bulunamadı veya boş."
        binding.ingredientsMezze.text = ingredients ?: "Malzeme bulunamadı veya boş."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG_MEZZE = "DailyMezzeFragment"
        private const val LAST_UPDATE_TIME_KEY_MEZZE = "son_guncelleme_zamani"
        private const val NAME_KEY_MEZZE = "isim"
        private const val DESCRIPTION_KEY_MEZZE = "aciklama"
        private const val IMAGE_URL_KEY_MEZZE = "resim_url"
        private const val RECIPES_KEY_MEZZE = "tarifler"
        private const val INGREDIENTS_KEY_MEZZE = "malzemeler"
    }
}
