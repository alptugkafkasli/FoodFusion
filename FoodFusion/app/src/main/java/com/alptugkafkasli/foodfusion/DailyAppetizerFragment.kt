import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alptugkafkasli.foodfusion.databinding.FragmentDailyAppetizerBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class DailyAppetizerFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private var lastUpdateTime: Long = 0
    private lateinit var db: FirebaseFirestore

    private var _binding: FragmentDailyAppetizerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDailyAppetizerBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("MyAppetizer", Context.MODE_PRIVATE)
        db = Firebase.firestore
        checkAndUpdateData()
        return binding.root
    }

    private fun checkAndUpdateData() {
        val currentTime = Calendar.getInstance().timeInMillis
        val lastUpdatedTime = sharedPreferences.getLong(LAST_UPDATE_TIME_KEY_APPETIZER, 0)

        if (currentTime - lastUpdatedTime >= 24 * 60 * 60 * 1000) {
            // Yeni veriyi al
            fetchDataFromFirestore()
        } else {
            // Saklanan veriyi kullan
            val name = sharedPreferences.getString(NAME_KEY_APPETIZER, "")
            val description = sharedPreferences.getString(DESCRIPTION_KEY_APPETIZER, "")
            val imageUrl = sharedPreferences.getString(IMAGE_URL_KEY_APPETIZER, "")
            val recipes = sharedPreferences.getString(RECIPES_KEY_APPETIZER, "")
            val ingredients = sharedPreferences.getString(INGREDIENTS_KEY_APPETIZER, "")

            // Arayüzü güncelle
            updateUI(name, description, imageUrl, recipes, ingredients)
        }
    }

    private fun fetchDataFromFirestore() {
        db.collection("Appetizer").get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d(TAG_APPETIZER, "Belge bulunamadı.")
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
                    putString(NAME_KEY_APPETIZER, name)
                    putString(DESCRIPTION_KEY_APPETIZER, description)
                    putString(IMAGE_URL_KEY_APPETIZER, imageUrl)
                    putString(RECIPES_KEY_APPETIZER, recipes?.joinToString("\n\n"))
                    putString(INGREDIENTS_KEY_APPETIZER, ingredients?.joinToString("\n\n"))
                    putLong(LAST_UPDATE_TIME_KEY_APPETIZER, Calendar.getInstance().timeInMillis)
                    apply()
                }

                // Arayüzü güncelle
                updateUI(name, description, imageUrl, recipes?.joinToString("\n\n"), ingredients?.joinToString("\n\n"))
            }
            .addOnFailureListener { exception ->
                Log.w(TAG_APPETIZER, "Belgeleri alma işlemi başarısız oldu.", exception)
            }
    }

    private fun updateUI(name: String?, description: String?, imageUrl: String?, recipes: String?, ingredients: String?) {
        binding.nameAppetizer.text = name
        binding.descriptionAppetizer.text = description

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(binding.imageAppetizer)
        } else {
            Log.e(TAG_APPETIZER, "ImageUrl boş veya null")
        }

        binding.recipesAppetizer.text = recipes ?: "Tarif bulunamadı veya boş."
        binding.ingredientsAppetizer.text = ingredients ?: "Malzeme bulunamadı veya boş."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG_APPETIZER = "DailyAppetizerFragment"
        private const val LAST_UPDATE_TIME_KEY_APPETIZER = "son_guncelleme_zamani"
        private const val NAME_KEY_APPETIZER = "isim"
        private const val DESCRIPTION_KEY_APPETIZER = "aciklama"
        private const val IMAGE_URL_KEY_APPETIZER = "resim_url"
        private const val RECIPES_KEY_APPETIZER = "tarifler"
        private const val INGREDIENTS_KEY_APPETIZER = "malzemeler"
    }
}
