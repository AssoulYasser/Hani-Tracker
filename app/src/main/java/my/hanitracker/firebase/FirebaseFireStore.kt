package my.hanitracker.firebase

import android.annotation.SuppressLint
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.lang.Exception

class FirebaseFireStore {
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

    fun getDocumentWhereDataEqualTo(collection: String, fieldName: String, fieldValue: String, onSuccess : (QuerySnapshot) -> Unit, onFailure : (Exception) -> Unit){
        firebaseFireStore.collection(collection).whereEqualTo(fieldName, fieldValue).get().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure)
    }

    fun ifDocumentExists(collection: String, document: String, onSuccess: (Boolean) -> Unit, onFailure : (Exception) -> Unit) {
        firebaseFireStore.collection(collection).document(document).get()
            .addOnSuccessListener { onSuccess(it.exists()) }
            .addOnFailureListener { onFailure(it) }
    }

}