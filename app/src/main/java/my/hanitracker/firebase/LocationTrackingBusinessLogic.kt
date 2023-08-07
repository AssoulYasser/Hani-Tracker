package my.hanitracker.firebase

import android.net.Uri
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.firestore.QueryDocumentSnapshot
import my.hanitracker.location.UserLocationDataClass
import my.hanitracker.user.UserDataClass

class LocationTrackingBusinessLogic {
    private val firebaseRealtimeStore = FirebaseRealtimeStore()
    private val firebaseFireStore = FirebaseFireStore()
    private lateinit var locationEventListener: ChildEventListener

    fun startTrackLocation(
        onAdd: (UserLocationDataClass) -> Unit,
        onChange: (uid: String, latitude: Double, longitude: Double) -> Unit,
        onDelete: (uid: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        locationEventListener = object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val id = snapshot.key!!
                val location = snapshot.value
                if (location is Map<*, *>)
                    firebaseFireStore.getDocumentWhereDataEqualTo(
                        collection = "user",
                        fieldName = "uid",
                        fieldValue = id,
                        onSuccess = { querySnapshot ->
                            querySnapshot.forEach {
                                val user = getUserData(it)
                                val userLocation = UserLocationDataClass(user = user, latitude = location["latitude"] as Double, longitude = location["longitude"] as Double)
                                onAdd(userLocation)
                            }
                        },
                        onFailure = onFailure,
                    )
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val id = snapshot.key!!
                val location = snapshot.value
                if(location is Map<*,*>)
                    onChange(id, location["latitude"] as Double, location["longitude"] as Double)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val id = snapshot.key!!
                onDelete(id)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        firebaseRealtimeStore.addDataEventListener("location", locationEventListener)

    }

    fun stopTrackingLocation() {
        firebaseRealtimeStore.deleteDataEventListener("location", locationEventListener)
    }

    private fun getUserData(documentSnapshot: QueryDocumentSnapshot) : UserDataClass {
        val email = documentSnapshot.id
        val firstName = documentSnapshot["first name"] as String
        val lastName = documentSnapshot["last name"] as String
        val uid = documentSnapshot["uid"] as String
        val pfpUri = Uri.parse(documentSnapshot["profile picture uri"] as String)
        return UserDataClass(uid = uid, firstName = firstName, lastName = lastName, email = email, pfp = pfpUri)
    }

}