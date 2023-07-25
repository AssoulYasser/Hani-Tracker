package my.hanitracker.firebase

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

object FirebaseFireStore {
    @SuppressLint("StaticFieldLeak")
    private val firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun storeData(data: HashMap<String, Any>, collection: String, document: String, onSuccess : () -> Unit, onFailure : (Exception) -> Unit ){
        firebaseFireStore.collection(collection).document(document).set(data)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun deleteData(collection: String, document: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        firebaseFireStore.collection(collection).document(document).delete().addOnSuccessListener { onSuccess() }.addOnFailureListener(onFailure)
    }

    fun getData(collection: String, document: String, onSuccess : (DocumentSnapshot) -> Unit, onFailure : (Exception) -> Unit){
        firebaseFireStore.collection(collection).document(document).get().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure)
    }

}