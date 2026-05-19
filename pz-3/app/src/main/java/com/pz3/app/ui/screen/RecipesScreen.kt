package com.pz3.app.ui.screen

import android.app.Application
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.pz3.app.data.db.AppDatabase
import com.pz3.app.data.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class RecipeJson(
    val name: String?,
    val ingredients: String?,
    val instructions: String?
)

class RecipesViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).recipeDao()

    val recipes: StateFlow<List<Recipe>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(name: String, ingredients: String, instructions: String) {
        viewModelScope.launch {
            dao.insert(Recipe(name = name, ingredients = ingredients, instructions = instructions))
        }
    }

    fun remove(recipe: Recipe) {
        viewModelScope.launch { dao.delete(recipe) }
    }

    fun exportRecipe(recipe: Recipe, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = Gson().toJson(RecipeJson(recipe.name, recipe.ingredients, recipe.instructions))
            getApplication<Application>().contentResolver.openOutputStream(uri)?.use {
                it.write(json.toByteArray(Charsets.UTF_8))
            }
        }
    }

    fun importRecipe(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = getApplication<Application>().contentResolver.openInputStream(uri)?.use {
                    it.readBytes().toString(Charsets.UTF_8)
                } ?: return@launch
                val r = Gson().fromJson(json, RecipeJson::class.java)
                val name = r.name?.takeIf { it.isNotBlank() } ?: return@launch
                dao.insert(Recipe(
                    name = name,
                    ingredients = r.ingredients ?: "",
                    instructions = r.instructions ?: ""
                ))
            } catch (_: Exception) { }
        }
    }
}

@Composable
fun RecipesScreen(vm: RecipesViewModel = viewModel()) {
    var showForm by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Recipe?>(null) }
    val recipes by vm.recipes.collectAsState()

    when {
        selected != null -> {
            val recipe = selected!!
            RecipeDetailView(
                recipe = recipe,
                onBack = { selected = null },
                onExport = { uri -> vm.exportRecipe(recipe, uri) }
            )
        }
        showForm -> AddRecipeForm(
            onSave = { name, ingredients, instructions ->
                vm.add(name, ingredients, instructions)
                showForm = false
            },
            onCancel = { showForm = false }
        )
        else -> RecipeListView(
            recipes = recipes,
            onAdd = { showForm = true },
            onSelect = { selected = it },
            onDelete = { vm.remove(it) },
            onImport = { uri -> vm.importRecipe(uri) }
        )
    }
}

@Composable
private fun RecipeListView(
    recipes: List<Recipe>,
    onAdd: () -> Unit,
    onSelect: (Recipe) -> Unit,
    onDelete: (Recipe) -> Unit,
    onImport: (Uri) -> Unit
) {
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { onImport(it) } }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Рецепти",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { importLauncher.launch(arrayOf("*/*")) }) {
                Icon(Icons.Default.FolderOpen, contentDescription = "Імпортувати рецепт")
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Додати рецепт")
            }
        }

        Spacer(Modifier.height(8.dp))

        if (recipes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Список рецептів порожній.\nНатисніть + щоб додати перший рецепт.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(recipes, key = { it.id }) { recipe ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onSelect(recipe) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                recipe.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = { onDelete(recipe) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Видалити")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeDetailView(recipe: Recipe, onBack: () -> Unit, onExport: (Uri) -> Unit) {
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri -> uri?.let { onExport(it) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Text(
                recipe.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { exportLauncher.launch("${recipe.name}.recipe") }) {
                Icon(Icons.Default.Share, contentDescription = "Експортувати рецепт")
            }
        }

        Text("Інгредієнти", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(recipe.ingredients)

        Spacer(Modifier.height(24.dp))

        Text("Інструкції приготування", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(recipe.instructions)
    }
}

@Composable
private fun AddRecipeForm(onSave: (String, String, String) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            IconButton(onClick = onCancel) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Скасувати")
            }
            Text("Новий рецепт", style = MaterialTheme.typography.headlineMedium)
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Назва рецепту") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = ingredients,
            onValueChange = { ingredients = it },
            label = { Text("Інгредієнти") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = instructions,
            onValueChange = { instructions = it },
            label = { Text("Інструкції приготування") },
            minLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { onSave(name, ingredients, instructions) },
            enabled = name.isNotBlank() && ingredients.isNotBlank() && instructions.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Зберегти рецепт")
        }
    }
}
