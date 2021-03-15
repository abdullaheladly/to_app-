package com.abdullah996.to_doapp.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavArgs
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.abdullah996.to_doapp.R
import com.abdullah996.to_doapp.data.models.Priority
import com.abdullah996.to_doapp.data.models.ToDoData
import com.abdullah996.to_doapp.data.viewmodel.ToDoViewModel
import com.abdullah996.to_doapp.databinding.FragmentUpdateBinding
import com.abdullah996.to_doapp.fragments.SharedViewModel
import kotlinx.android.synthetic.main.fragment_update.*
import kotlinx.android.synthetic.main.fragment_update.view.*


class UpdateFragment : Fragment() {
    private val args by navArgs<UpdateFragmentArgs>()

    private val mSharedViewModel:SharedViewModel by viewModels()

    private val mToDoViewModel:ToDoViewModel by viewModels()

    private var _binding:FragmentUpdateBinding?=null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // data binding
        _binding= FragmentUpdateBinding.inflate(inflater,container,false)
        binding.args=args
        setHasOptionsMenu(true)

        //spinnerItemSelectedListener
        binding.currentPrioritiesSpinner.onItemSelectedListener=mSharedViewModel.listener

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menu_save-> updateItem()
            R.id.menu_delete-> confirmItemRemoval()
        }
        return super.onOptionsItemSelected(item)
    }




    private fun updateItem() {
        val title=current_title_et.text.toString()
        val description=current_description_et.text.toString()
        val getPriority=current_priorities_spinner.selectedItem.toString()

        val validation=mSharedViewModel.verifyDataFromUser(title,description)
        if (validation){
            // update current item
            val updateItem=ToDoData(
                    args.currentitem.id,
                    title,
                    mSharedViewModel.parsePriority(getPriority),
                    description
            )
            mToDoViewModel.updateData(updateItem)
            Toast.makeText(requireContext(),"successfully updated",Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }else{
            Toast.makeText(requireContext()," please fill all fields ",Toast.LENGTH_SHORT).show()

        }
    }

    // show alert dialog to confirm delete
    private fun confirmItemRemoval() {
        val builder =AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes"){_,_->
            mToDoViewModel.deleteItem(args.currentitem)
            Toast.makeText(requireContext(),"Successfully removed : '${args.currentitem.title}'",Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        builder.setNegativeButton("No"){_,_ ->}
        builder.setTitle("Delete  '${args.currentitem.title}' ?")
        builder.setMessage("Are you sure you want to delete '${args.currentitem.title}' ?")
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }


}