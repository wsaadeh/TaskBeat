package com.saadeh.taskbeat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var categories = listOf<CategoryUiData>()
    private var categoriesEntity = listOf<CategoryEntity>()
    private var tasks = listOf<TaskUiData>()

    private lateinit var rvCategory: RecyclerView
    private lateinit var ctnEmptyView: LinearLayout
    private lateinit var fabCreateTask: FloatingActionButton

    private val categoryAdapter = CategoryListAdapter()
    private val taskAdapter by lazy {
        TaskListAdapter()
    }

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            TaskBeatDatabase::class.java, "database-taskbeat"
        ).build()
    }

    private val categoryDao by lazy {
        db.getCategoryDao()
    }

    private val taskDao by lazy {
        db.getTaskDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //insertDefaultCategory()
        //insertDefaultTask()

        rvCategory = findViewById(R.id.rv_categories)
        ctnEmptyView = findViewById(R.id.ll_empty_view)
        val rvTask = findViewById<RecyclerView>(R.id.rv_tasks)
        fabCreateTask = findViewById(R.id.fab_create_task)
        val btnCreateEmpty = findViewById<Button>(R.id.btn_create_empty)

        btnCreateEmpty.setOnClickListener() {
            showCreateCategoryBottomSheet()
        }

        fabCreateTask.setOnClickListener() {
            showCreateUpdateTaskBottomSheet()
        }

        taskAdapter.setOnClickListener { task ->
            showCreateUpdateTaskBottomSheet(task)
        }

        categoryAdapter.setOnLongClickListener { categoryToBeDeleted ->
            if (categoryToBeDeleted.name != "+" && categoryToBeDeleted.name != "ALL") {
                val title: String = this.getString(R.string.category_delete_title)
                val description: String = this.getString(R.string.category_delete_description)
                val btnText: String = this.getString(R.string.delete)
                showInfoDialog(
                    title,
                    description,
                    btnText
                ) {
                    val categoryEntityToBeDeleted = CategoryEntity(
                        categoryToBeDeleted.name,
                        categoryToBeDeleted.isSelected
                    )
                    deleteCategory(categoryEntityToBeDeleted)
                }
            }
        }

        categoryAdapter.setOnClickListener { selected ->
            if (selected.name == "+") {
                //Snackbar.make(rvCategory, "+ is selected", Snackbar.LENGTH_LONG).show()
                showCreateCategoryBottomSheet()
            } else {
                val categoryTemp = categories.map { item ->
                    when {
                        item.name == selected.name && !item.isSelected -> item.copy(isSelected = true)

                        //item.name == selected.name && item.isSelected -> item.copy(isSelected = true)
                        item.name != selected.name && item.isSelected -> item.copy(isSelected = false)
                        else -> item
                    }
                }


                //val taskTemp =
                if (selected.name != "ALL") {
                    //tasks.filter { it.category == selected.name }
                    GlobalScope.launch(Dispatchers.IO) {
                        filterTaskByCategoryName(selected.name)
                    }
                } else {
                    //tasks
                    GlobalScope.launch(Dispatchers.IO) {
                        getTasksFromDatabase()
                    }
                }
                //taskAdapter.submitList(tasks)

                categoryAdapter.submitList(categoryTemp)
            }

        }

        rvCategory.adapter = categoryAdapter
        GlobalScope.launch(Dispatchers.IO) {
            getCategoriesFromDatabase()//categoryAdapter
        }
        //categoryAdapter.submitList(categories)

        rvTask.adapter = taskAdapter
        GlobalScope.launch(Dispatchers.IO) {
            getTasksFromDatabase()
        }
        //taskAdapter.submitList(tasks)
    }

    private fun insertDefaultCategory() {
        val categoriesEntity = categories.map {
            CategoryEntity(
                name = it.name,
                isSelected = it.isSelected
            )
        }

        GlobalScope.launch(Dispatchers.IO) {
            categoryDao.insertAll(categoriesEntity)
        }

    }

    private fun insertDefaultTask() {
        val tasksEntities = tasks.map {
            TaskEntity(
                category = it.category,
                name = it.name
            )
        }

        GlobalScope.launch(Dispatchers.IO) {
            taskDao.insertAll(tasksEntities)
        }
    }

    //adapter: CategoryListAdapter
    private fun getCategoriesFromDatabase() {

        val categoriesFromDb: List<CategoryEntity> = categoryDao.getAll()
        categoriesEntity = categoriesFromDb

        GlobalScope.launch(Dispatchers.Main) {
            if (categoriesEntity.isEmpty()) {
                rvCategory.isVisible = false
                fabCreateTask.isVisible = false
                ctnEmptyView.isVisible = true
            } else {
                rvCategory.isVisible = true
                fabCreateTask.isVisible = true
                ctnEmptyView.isVisible = false
            }
        }
        val categoriesUIData = categoriesFromDb.map {
            CategoryUiData(
                name = it.name,
                isSelected = it.isSelected
            )
        }.toMutableList()
        //Add fake + category
        categoriesUIData.add(
            CategoryUiData(
                name = "+",
                isSelected = false
            )
        )
        val categoryListTemp = mutableListOf(
            CategoryUiData(
                name = "ALL",
                isSelected = true
            )
        )

        categoryListTemp.addAll(categoriesUIData)
        GlobalScope.launch(Dispatchers.Main) {
            categories = categoryListTemp
            categoryAdapter.submitList(categories)//adapter
        }

    }

    private fun getTasksFromDatabase() {//adapter: TaskListAdapter

        val tasksFromDb: List<TaskEntity> = taskDao.getAll()
        val tasksUIData: List<TaskUiData> = tasksFromDb.map {
            TaskUiData(
                id = it.id,
                name = it.name,
                category = it.category
            )
        }
        GlobalScope.launch(Dispatchers.Main) {
            tasks = tasksUIData
            taskAdapter.submitList(tasksUIData)//adapter
        }

    }

    private fun insertCategory(categoryEntity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            categoryDao.insert(categoryEntity)
            getCategoriesFromDatabase()
        }
    }

    private fun insertTask(taskEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.insert(taskEntity)
            getTasksFromDatabase()
        }
    }

    private fun updateTask(taskEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.update(taskEntity)
            getTasksFromDatabase()
            //filterTaskByCategoryName(taskEntity.category)
        }
    }

    private fun deleteTask(taskEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.delete(taskEntity)
            getTasksFromDatabase()
        }
    }

    private fun deleteCategory(categoryEntity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            val tasksToBeDeleted = taskDao.getAllByCategoryName(categoryEntity.name)
            taskDao.deleteAll(tasksToBeDeleted)
            categoryDao.delete(categoryEntity)
            getCategoriesFromDatabase()
            getTasksFromDatabase()
        }
    }

    private fun filterTaskByCategoryName(category: String) {

        val tasksFromDb: List<TaskEntity> = taskDao.getAllByCategoryName(category)
        val tasksUiData: List<TaskUiData> = tasksFromDb.map {
            TaskUiData(
                id = it.id,
                name = it.name,
                category = it.category
            )
        }

        GlobalScope.launch(Dispatchers.Main) {
            //tasks = tasksUiData
            taskAdapter.submitList(tasksUiData)
        }

    }

    private fun showInfoDialog(
        title: String,
        description: String,
        btnText: String,
        onClick: () -> Unit
    ) {
        val infoBottomSheet = InfoBottomSheet(
            title = title,
            description = description,
            btnText = btnText,
            onClicked = onClick
        )
        infoBottomSheet.show(
            supportFragmentManager,
            "infoBottomSheet"
        )
    }

    private fun showCreateUpdateTaskBottomSheet(taskUiData: TaskUiData? = null) {
        val createTaskBottomSheet = CreateOrUpdateTaskBottomSheet(
            task = taskUiData,
            categoryList = categoriesEntity,
            onCreateClicked = { taskToBeCreated ->
                val taskEntityToBeInsert = TaskEntity(
                    name = taskToBeCreated.name,
                    category = taskToBeCreated.category
                )
                insertTask(taskEntityToBeInsert)
            }, onUpdateClicked = { taskToBeUpdated ->
                val taskEntityToBeUpdated = TaskEntity(
                    id = taskToBeUpdated.id,
                    name = taskToBeUpdated.name,
                    category = taskToBeUpdated.category
                )
                updateTask(taskEntityToBeUpdated)
            }, onDeleteClicked = { taskToBeDeleted ->
                val taskEntityToBeDeleted = TaskEntity(
                    id = taskToBeDeleted.id,
                    name = taskToBeDeleted.name,
                    category = taskToBeDeleted.category
                )
                deleteTask(taskEntityToBeDeleted)
            }
        )

        createTaskBottomSheet.show(
            supportFragmentManager,
            "createTaskBottomSheet"
        )
    }

    private fun showCreateCategoryBottomSheet() {
        val createCategoryBottomSheet = CreateCategoryBottomSheet { categoryName ->
            val categoryEntity = CategoryEntity(
                name = categoryName,
                isSelected = false
            )
            insertCategory(categoryEntity)
        }
        createCategoryBottomSheet.show(supportFragmentManager, "createCategoryBottomSheet")
    }
}

/*val categories = listOf(
    CategoryUiData(
        name = "ALL",
        isSelected = false
    ),
    CategoryUiData(
        name = "STUDY",
        isSelected = false
    ),
    CategoryUiData(
        name = "WORK",
        isSelected = false
    ),
    CategoryUiData(
        name = "WELLNESS",
        isSelected = false
    ),
    CategoryUiData(
        name = "HOME",
        isSelected = false
    ),
    CategoryUiData(
        name = "HEALTH",
        isSelected = false
    ),
)*/

/*val tasks = listOf(
    TaskUiData(
        "Ler 10 páginas do livro atual",
        "STUDY"
    ),
    TaskUiData(
        "45 min de treino na academia",
        "HEALTH"
    ),
    TaskUiData(
        "Correr 5km",
        "HEALTH"
    ),
    TaskUiData(
        "Meditar por 10 min",
        "WELLNESS"
    ),
    TaskUiData(
        "Silêncio total por 5 min",
        "WELLNESS"
    ),
    TaskUiData(
        "Descer o livo",
        "HOME"
    ),
    TaskUiData(
        "Tirar caixas da garagem",
        "HOME"
    ),
    TaskUiData(
        "Lavar o carro",
        "HOME"
    ),
    TaskUiData(
        "Gravar aulas DevSpace",
        "WORK"
    ),
    TaskUiData(
        "Criar planejamento de vídeos da semana",
        "WORK"
    ),
    TaskUiData(
        "Soltar reels da semana",
        "WORK"
    ),
)*/