package com.example.fisiobotkids.data.repository

import android.util.Log
import com.example.fisiobotkids.data.model.*
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class FirebaseFisioBotRepository : FisioBotRepository {

    override var doctorUid: String? = null
    private val db = FirebaseDatabase.getInstance()

    private fun pacientesRef(): DatabaseReference? =
        doctorUid?.let { db.getReference("doctores/$it/pacientes") }

    // Leer sensores del robot (ruta robot/{childId}/sensores)
    override fun getSensorData(childId: String): Flow<SensorData> = callbackFlow {
        val ref = db.getReference("robot/$childId/sensores")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(SensorData::class.java)
                if (data != null) trySend(data)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun getRobotState(childId: String): Flow<RobotState> = callbackFlow {
        val ref = db.getReference("robot/$childId/estado")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val state = snapshot.getValue(RobotState::class.java)
                if (state != null) trySend(state)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun enviarComandos(childId: String, comandos: Comandos) {
        db.getReference("robot/$childId/comandos").setValue(comandos)
    }

    // Obtener lista de niños del doctor actual
    override fun getChildrenList(): Flow<List<Child>> = callbackFlow {
        val ref = pacientesRef()
        if (ref == null) {
            // Cerramos el flujo limpiamente en lugar de lanzar excepción
            close()
            return@callbackFlow
        }
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children.map { snap ->
                    Child(
                        id = snap.key ?: "",
                        nombre = snap.child("nombre").getValue(String::class.java) ?: "",
                        edad = snap.child("edad").getValue(Int::class.java) ?: 0,
                        codigo = snap.child("codigo").getValue(String::class.java) ?: "",
                        doctorUid = doctorUid
                    )
                }
                trySend(children)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    // Agregar un niño al doctor actual, guardando su perfil
    override suspend fun addChild(childId: String, nombre: String, edad: Int) {
        val ref = pacientesRef() ?: throw IllegalStateException("Doctor no autenticado")
        val codigo = (1000..9999).random().toString()
        val childData = mapOf(
            "nombre" to nombre,
            "edad" to edad,
            "codigo" to codigo,
            "fechaRegistro" to ServerValue.TIMESTAMP
        )
        ref.child(childId).setValue(childData).addOnSuccessListener {
            Log.d("FirebaseRepo", "Niño $childId registrado correctamente")
        }.addOnFailureListener { e ->
            Log.e("FirebaseRepo", "Error al registrar niño", e)
        }
    }

    // Buscar childId por código (para acceso sin login, si se desea conservar)
    override suspend fun getChildIdByCode(code: String): String? {
        return try {
            val snap = db.getReference("codigos/$code").get().await()
            snap.child("childId").getValue(String::class.java)
        } catch (e: Exception) {
            null
        }
    }
}