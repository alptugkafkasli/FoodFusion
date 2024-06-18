import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alptugkafkasli.foodfusion.databinding.FragmentDailySaladBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class DailySaladFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private var lastUpdateTime: Long = 0
    private lateinit var db: FirebaseFirestore

    private var _binding: FragmentDailySaladBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDailySaladBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("MySalad", Context.MODE_PRIVATE)
        db = Firebase.firestore
        checkAndUpdateData()
        return binding.root
    }

    private fun checkAndUpdateData() {
        val currentTime = Calendar.getInstance().timeInMillis
        val lastUpdatedTime = sharedPreferences.getLong(LAST_UPDATE_TIME_KEY_SALAD, 0)

        if (currentTime - lastUpdatedTime >= 24 * 60 * 60 * 1000) {
            // Yeni veriyi al
            fetchDataFromFirestore()
        } else {
            // Saklanan veriyi kullan
            val name = sharedPreferences.getString(NAME_KEY_SALAD, "")
            val description = sharedPreferences.getString(DESCRIPTION_KEY_SALAD, "")
            val imageUrl = sharedPreferences.getString(IMAGE_URL_KEY_SALAD, "")
            val recipes = sharedPreferences.getString(RECIPES_KEY_SALAD, "")
            val ingredients = sharedPreferences.getString(INGREDIENTS_KEY_SALAD, "")

            // Arayüzü güncelle
            updateUI(name, description, imageUrl, recipes, ingredients)
        }
    }

    private fun fetchDataFromFirestore() {
        db.collection("Salad").get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d(TAG_SALAD, "Belge bulunamadı.")
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
                    putString(NAME_KEY_SALAD, name)
                    putString(DESCRIPTION_KEY_SALAD, description)
                    putString(IMAGE_URL_KEY_SALAD, imageUrl)
                    putString(RECIPES_KEY_SALAD, recipes?.joinToString("\n\n"))
                    putString(INGREDIENTS_KEY_SALAD, ingredients?.joinToString("\n\n"))
                    putLong(LAST_UPDATE_TIME_KEY_SALAD, Calendar.getInstance().timeInMillis)
                    apply()
                }

                // Arayüzü güncelle
                updateUI(name, description, imageUrl, recipes?.joinToString("\n\n"), ingredients?.joinToString("\n\n"))
            }
            .addOnFailureListener { exception ->
                Log.w(TAG_SALAD, "Belgeleri alma işlemi başarısız oldu.", exception)
            }
    }

    private fun updateUI(name: String?, description: String?, imageUrl: String?, recipes: String?, ingredients: String?) {
        binding.nameSalad.text = name
        binding.descriptionSalad.text = description

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(binding.imageSalad)
        } else {
            Log.e(TAG_SALAD, "ImageUrl boş veya null")
        }

        binding.recipesSalad.text = recipes ?: "Tarif bulunamadı veya boş."
        binding.ingredientsSalad.text = ingredients ?: "Malzeme bulunamadı veya boş."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG_SALAD = "DailySaladFragment"
        private const val LAST_UPDATE_TIME_KEY_SALAD = "son_guncelleme_zamani"
        private const val NAME_KEY_SALAD = "isim"
        private const val DESCRIPTION_KEY_SALAD = "aciklama"
        private const val IMAGE_URL_KEY_SALAD = "resim_url"
        private const val RECIPES_KEY_SALAD = "tarifler"
        private const val INGREDIENTS_KEY_SALAD = "malzemeler"
    }
}
