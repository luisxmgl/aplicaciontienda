package com.example.aplicaciontienda.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.example.aplicaciontienda.ui.theme.BackgroundPaperRaised
import com.example.aplicaciontienda.ui.theme.SuccessGreen
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CARGOS = listOf("Vendedora", "Dueño(a) del local")
private val ESTADOS = listOf("activo", "vacaciones", "inactivo")

data class Empleado(
    val id: String = "",
    val nombre: String = "",
    val cargo: String = "",
    val telefono: String = "",
    val fechaIngreso: String = "",
    val sueldoSimulado: Long = 0,
    val rol: String = "vendedor",
    val estado: String = "activo",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

/** Equivalente a AdminHR.jsx: CRUD completo (crear/editar/listar/eliminar) de rrhh/empleados directo a Firebase RTDB. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHRScreen(onBack: () -> Unit) {
    val empleados = remember { mutableStateListOf<Empleado>() }
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Empleado?>(null) }
    var deleting by remember { mutableStateOf<Empleado?>(null) }

    DisposableEffect(Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("rrhh/empleados")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    val e = child.getValue(Empleado::class.java) ?: return@mapNotNull null
                    e.copy(id = child.key ?: "")
                }.sortedByDescending { it.createdAt }
                empleados.clear()
                empleados.addAll(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RRHH") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showForm = true }) { Icon(Icons.Default.Add, contentDescription = "Agregar empleado") }
        }
    ) { padding ->
        if (empleados.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Aún no hay empleados registrados.")
            }
        } else {
            LazyColumn(Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(empleados, key = { it.id }) { empleado ->
                    Card(
                        onClick = { editing = empleado; showForm = true },
                        colors = CardDefaults.cardColors(containerColor = BackgroundPaperRaised)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text(empleado.nombre, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(
                                    empleado.estado,
                                    color = if (empleado.estado == "activo") SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                IconButton(onClick = { deleting = empleado }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            Text(empleado.cargo, style = MaterialTheme.typography.bodySmall)
                            if (empleado.telefono.isNotEmpty()) Text(empleado.telefono, style = MaterialTheme.typography.bodySmall)
                            if (empleado.fechaIngreso.isNotEmpty()) Text("Ingreso: ${empleado.fechaIngreso}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        EmpleadoFormDialog(
            initial = editing,
            onDismiss = { showForm = false },
            onSave = { empleado ->
                val ref = FirebaseDatabase.getInstance().getReference("rrhh/empleados")
                val now = System.currentTimeMillis()
                if (empleado.id.isEmpty()) {
                    val newRef = ref.push()
                    newRef.setValue(empleado.copy(createdAt = now, updatedAt = now))
                } else {
                    ref.child(empleado.id).setValue(empleado.copy(updatedAt = now))
                }
                showForm = false
            }
        )
    }

    deleting?.let { empleado ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Eliminar empleado") },
            text = { Text("¿Eliminar a ${empleado.nombre}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseDatabase.getInstance().getReference("rrhh/empleados").child(empleado.id).removeValue()
                    if (editing?.id == empleado.id) { editing = null; showForm = false }
                    deleting = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancelar") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmpleadoFormDialog(initial: Empleado?, onDismiss: () -> Unit, onSave: (Empleado) -> Unit) {
    var nombre by remember { mutableStateOf(initial?.nombre ?: "") }
    var cargo by remember { mutableStateOf(initial?.cargo ?: CARGOS.first()) }
    var telefono by remember { mutableStateOf(initial?.telefono ?: "") }
    var fechaIngreso by remember { mutableStateOf(initial?.fechaIngreso ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var estado by remember { mutableStateOf(initial?.estado ?: "activo") }
    var cargoMenuExpanded by remember { mutableStateOf(false) }
    var estadoMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(20.dp)) {
                Text(if (initial == null) "Nuevo empleado" else "Editar empleado", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = cargoMenuExpanded, onExpandedChange = { cargoMenuExpanded = it }) {
                    OutlinedTextField(
                        value = cargo, onValueChange = {}, readOnly = true, label = { Text("Cargo") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = cargoMenuExpanded, onDismissRequest = { cargoMenuExpanded = false }) {
                        CARGOS.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { cargo = c; cargoMenuExpanded = false }) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = fechaIngreso, onValueChange = { fechaIngreso = it }, label = { Text("Fecha de ingreso (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = estadoMenuExpanded, onExpandedChange = { estadoMenuExpanded = it }) {
                    OutlinedTextField(
                        value = estado, onValueChange = {}, readOnly = true, label = { Text("Estado") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = estadoMenuExpanded, onDismissRequest = { estadoMenuExpanded = false }) {
                        ESTADOS.forEach { e -> DropdownMenuItem(text = { Text(e) }, onClick = { estado = e; estadoMenuExpanded = false }) }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nombre.isNotBlank()) {
                                onSave(
                                    Empleado(
                                        id = initial?.id ?: "",
                                        nombre = nombre.trim(),
                                        cargo = cargo,
                                        telefono = telefono.trim(),
                                        fechaIngreso = fechaIngreso.trim(),
                                        sueldoSimulado = initial?.sueldoSimulado ?: 0,
                                        rol = "vendedor",
                                        estado = estado,
                                        createdAt = initial?.createdAt ?: 0
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentThread)
                    ) { Text("Guardar") }
                }
            }
        }
    }
}
