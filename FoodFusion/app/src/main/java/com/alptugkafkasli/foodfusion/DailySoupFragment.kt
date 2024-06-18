import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alptugkafkasli.foodfusion.databinding.FragmentDailySoupBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class DailySoupFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private var lastUpdateTime: Long = 0
    private lateinit var db: FirebaseFirestore

    private var _binding: FragmentDailySoupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDailySoupBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("MySoup", Context.MODE_PRIVATE)
        db = Firebase.firestore
        checkAndUpdateData()
        return binding.root
    }

    private fun checkAndUpdateData() {
        val currentTime = Calendar.getInstance().timeInMillis
        val lastUpdatedTime = sharedPreferences.getLong(LAST_UPDATE_TIME_KEY, 0)

        if (currentTime - lastUpdatedTime >= 24 * 60 * 60 * 1000) {
            // Yeni veriyi al
            fetchDataFromFirestore()
        } else {
            // Saklanan veriyi kullan
            val name = sharedPreferences.getString(NAME_KEY, "")
            val description = sharedPreferences.getString(DESCRIPTION_KEY, "")
            val imageUrl = sharedPreferences.getString(IMAGE_URL_KEY, "")
            val recipes = sharedPreferences.getString(RECIPES_KEY, "")
            val ingredients = sharedPreferences.getString(INGREDIENTS_KEY, "")

            // Arayüzü güncelle
            updateUI(name, description, imageUrl, recipes, ingredients)
        }
    }

    private fun fetchDataFromFirestore() {
        db.collection("Soup").get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d(TAG, "Belge bulunamadı.")
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
                    putString(NAME_KEY, name)
                    putString(DESCRIPTION_KEY, description)
                    putString(IMAGE_URL_KEY, imageUrl)
                    putString(RECIPES_KEY, recipes?.joinToString("\n\n"))
                    putString(INGREDIENTS_KEY, ingredients?.joinToString("\n\n"))
                    putLong(LAST_UPDATE_TIME_KEY, Calendar.getInstance().timeInMillis)
                    apply()
                }

                // Arayüzü güncelle
                updateUI(name, description, imageUrl, recipes?.joinToString("\n\n"), ingredients?.joinToString("\n\n"))
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Belgeleri alma işlemi başarısız oldu.", exception)
            }
    }

    private fun updateUI(name: String?, description: String?, imageUrl: String?, recipes: String?, ingredients: String?) {
        binding.nameSoup.text = name
        binding.descriptionSoup.text = description

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(binding.imageSoup)
        } else {
            Log.e(TAG, "ImageUrl boş veya null")
        }

        binding.recipesSoup.text = recipes ?: "Tarif bulunamadı veya boş."
        binding.ingredientsSoup.text = ingredients ?: "Malzeme bulunamadı veya boş."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "DailySoupFragment"
        private const val LAST_UPDATE_TIME_KEY = "son_guncelleme_zamani"
        private const val NAME_KEY = "isim"
        private const val DESCRIPTION_KEY = "aciklama"
        private const val IMAGE_URL_KEY = "resim_url"
        private const val RECIPES_KEY = "tarifler"
        private const val INGREDIENTS_KEY = "malzemeler"
    }
}
