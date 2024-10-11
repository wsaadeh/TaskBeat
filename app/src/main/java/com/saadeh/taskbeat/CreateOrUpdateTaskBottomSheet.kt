package com.saadeh.taskbeat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class CreateOrUpdateTaskBottomSheet(
    private val categoryList: List<CategoryEntity>,
    private val task: TaskUiData? = null,
    private val onCreateClicked: (TaskUiData) -> Unit,
    private val onUpdateClicked: (TaskUiData) -> Unit,
    private val onDeleteClicked: (TaskUiData) -> Unit

) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.create_or_update_task_bottom_sheet, container, false)

        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val btnCreateOrUpdate = view.findViewById<Button>(R.id.btn_task_create_or_update)
        val btnDelete = view.findViewById<Button>(R.id.btn_task_delete)
        val tieTaskName = view.findViewById<TextInputEditText>(R.id.tie_task_name)
        val spinnerCat = view.findViewById<Spinner>(R.id.spin_category)

        var taskCategory: String? = null
        val categoryListTemp = mutableListOf("Select")
        categoryListTemp.addAll(
            categoryList.map { it.name }
        )
        val categoryStr: List<String> = categoryListTemp//categoryList.map { it.name }


        ArrayAdapter(
            requireActivity().baseContext,
            android.R.layout.simple_spinner_item,
            categoryStr.toList()
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCat.adapter = adapter
        }

        spinnerCat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                taskCategory = categoryStr[position]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        if (task == null) {
            tvTitle.setText(R.string.create_task_title)
            btnCreateOrUpdate.setText(R.string.create)
            //btnDelete.visibility = View.INVISIBLE
            btnDelete.isVisible = false
        } else {
            tvTitle.setText(R.string.update_task_title)
            btnCreateOrUpdate.setText(R.string.update)
            //btnDelete.visibility = View.VISIBLE
            btnDelete.isVisible = true

            tieTaskName.setText(task.name)
            //val currentCategory = categoryList.first() { it.name == task.category }
            //val index = categoryList.indexOf(currentCategory)
            val indexC = categoryListTemp.indexOf(task.category)
            spinnerCat.setSelection(indexC)
        }

        btnDelete.setOnClickListener(){
            if (task != null) {
                onDeleteClicked.invoke(task)
                dismiss()
            }else{
                Log.d("CreateOrUpdateTaskBottomSheet","Task not found")
            }
        }

        btnCreateOrUpdate.setOnClickListener() {
            val name = tieTaskName.text.toString().trim()
            if (taskCategory != "Select" && name.isNotEmpty()) {

                if (task == null) {
                    onCreateClicked.invoke(
                        TaskUiData(
                            id = 0,
                            name = tieTaskName.text.toString(),
                            category = requireNotNull(taskCategory)
                        )
                    )
                } else {
                    onUpdateClicked.invoke(
                        TaskUiData(
                            id = task.id,
                            name = tieTaskName.text.toString(),
                            category = requireNotNull(taskCategory)
                        )
                    )
                }

                dismiss()
            } else {
                Snackbar.make(btnCreateOrUpdate, "Please select a category", Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        return view
    }

}