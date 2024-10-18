package com.example.todoapp.fragments.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.todoapp.R
import com.example.todoapp.data.models.ToDoData
import com.example.todoapp.data.viewmodel.ToDoViewModel
import com.example.todoapp.databinding.FragmentAddBinding
import com.example.todoapp.fragments.SharedViewModel
import java.util.*

class AddFragment : Fragment() {

    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(layoutInflater, container, false)

        // Spinner Item Selected Listener
        binding.prioritiesSpinner.onItemSelectedListener = mSharedViewModel.listener

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Menu for adding tasks
        val menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.add_fragment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.menu_add) {
                    selectDueDateTime()
                } else if (menuItem.itemId == android.R.id.home) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    // Function to handle date and time selection
    private fun selectDueDateTime() {
        val calendar = Calendar.getInstance()

        // Date Picker
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Time Picker after selecting the date
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)

                        // Now we have a full timestamp in milliseconds
                        val dueTimeInMillis = calendar.timeInMillis
                        insertDataToDb(dueTimeInMillis)

                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Insert the task data along with the due time in milliseconds
    private fun insertDataToDb(dueTimeInMillis: Long) {
        val title = binding.titleEt.text.toString()
        val priority = binding.prioritiesSpinner.selectedItem.toString()
        val description = binding.descriptionEt.text.toString()

        // Validate user input
        val validation = mSharedViewModel.verifyDataFromUser(title, description)
        if (validation) {
            // Insert Data to Database
            val newData = ToDoData(
                0,
                title,
                mSharedViewModel.parsePriority(priority),
                description,
                dueTimeInMillis  // Pass the selected time
            )
            mToDoViewModel.insertData(newData)
            Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_SHORT).show()
            // Navigate Back to List
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        } else {
            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
