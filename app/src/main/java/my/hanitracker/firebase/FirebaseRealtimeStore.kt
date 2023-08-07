package my.hanitracker.firebase

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class FirebaseRealtimeStore {
    private val firebaseRealtimeStore = FirebaseDatabase.getInstance().reference

    fun storeData(data: Any?, path: String, onSuccess : () -> Unit, onFailure : (Exception) -> Unit) {
        firebaseRealtimeStore.child(path).setValue(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getData(path: String, onSuccess: (MutableIterable<DataSnapshot>) -> Unit, onFailure: (Exception) -> Unit) {
        Log.d("DEBUGGING : ", "getData: START")
        firebaseRealtimeStore.child(path).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("DEBUGGING : ", "getData: SUCCESS")
                    onSuccess(snapshot.children)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DEBUGGING : ", "getData: FAILED")
                    onFailure(error.toException())
                }

            }
        )

        Log.d("DEBUGGING : ", "getData: END")
    }


    fun addDataEventListener(path: String, childEventListener: ChildEventListener ) =
        firebaseRealtimeStore.child(path).addChildEventListener(childEventListener)

    fun deleteDataEventListener(path: String, childEventListener: ChildEventListener) {
        firebaseRealtimeStore.child(path).removeEventListener(childEventListener)
    }


}