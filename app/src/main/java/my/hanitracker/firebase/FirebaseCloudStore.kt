package my.hanitracker.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.Exception

class FirebaseCloudStore {
    private val firebaseCloudStore: StorageReference = FirebaseStorage.getInstance().reference
    private lateinit var reference : StorageReference

    fun uploadFile(fileToUpload: Uri, location: String, onSuccess : (Uri?) -> Unit, onFailure : (Exception) -> Unit) {
        reference = firebaseCloudStore.child(location)
        reference.putFile(fileToUpload).addOnCompleteListener { task ->
            if (task.isSuccessful)
                reference.downloadUrl.addOnSuccessListener(onSuccess).addOnFailureListener(onFailure)
            else
                task.exception?.let { onFailure(it) }
        }
    }

    fun getFileUri(location: String, onSuccess : (Uri?) -> Unit, onFailure : (Exception) -> Unit){
        reference = firebaseCloudStore.child(location)
        reference.downloadUrl.addOnSuccessListener(onSuccess).addOnFailureListener(onFailure)
    }


}