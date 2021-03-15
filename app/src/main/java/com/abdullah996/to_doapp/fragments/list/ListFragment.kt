package com.abdullah996.to_doapp.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.abdullah996.to_doapp.R
import com.abdullah996.to_doapp.data.models.ToDoData
import com.abdullah996.to_doapp.data.viewmodel.ToDoViewModel
import com.abdullah996.to_doapp.databinding.FragmentListBinding
import com.abdullah996.to_doapp.fragments.SharedViewModel
import com.abdullah996.to_doapp.fragments.list.adapter.ListAdapter
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import java.text.FieldPosition


class ListFragment : Fragment(),SearchView.OnQueryTextListener{


    private  val mToDoViewModel:ToDoViewModel by viewModels()
    private val adapter: ListAdapter by lazy { ListAdapter() }

    private var _binding:FragmentListBinding?=null
    private val binding get() = _binding!!

    private val mSharedViewModel:SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // data binding
        _binding= FragmentListBinding.inflate(inflater,container,false)
        binding.lifecycleOwner=this
        binding.mSharedViewModel=mSharedViewModel

        // setup recycle view
        setupRecycleView()

        // observing liveData
        mToDoViewModel.getAllData.observe(viewLifecycleOwner,Observer{ data->
            mSharedViewModel.checkIfDatabaseIfEmpty(data)
            adapter.setData(data)

        })

        //set menu
        setHasOptionsMenu(true)

        return binding.root
    }

    private fun setupRecycleView() {
        val recyclerView=binding.recyclerView
        recyclerView.adapter=adapter
        recyclerView.layoutManager=LinearLayoutManager(requireActivity())
        recyclerView.itemAnimator=SlideInLeftAnimator().apply {
            addDuration=300
        }

        // swipe to delete
        swipeToDelete(recyclerView)
    }


    private fun swipeToDelete(recyclerView: RecyclerView){
        val swipeToDeleteCallback=object :SwipeToDelete(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem=adapter.dataList[viewHolder.adapterPosition]
                mToDoViewModel.deleteItem(deletedItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                //restore deleted item
                restoreDeletedData(viewHolder.itemView,deletedItem,viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper=ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedData(view:View,deletedItem: ToDoData,position: Int){
        val snackbar=Snackbar.make(
            view,
            "deleted ${deletedItem.title}",
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction("undo"){
            mToDoViewModel.insertData(deletedItem)
            adapter.notifyItemChanged(position)
        }
        snackbar.show()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu,menu)
        val search=menu.findItem(R.id.menu_search)
        val searchView=search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled=true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menu_delete_all-> confirmRemoval()
            R.id.menu_priority_high->mToDoViewModel.sortByHighPriority.observe(this, Observer {
                adapter.setData(it)
            })
            R.id.menu_priority_low->mToDoViewModel.sortByLowPriority.observe(this, Observer {
                adapter.setData(it)
            })
        }
        return super.onOptionsItemSelected(item)
    }

    // show alert dialog to confirm delete everything
    private fun confirmRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes"){_,_->
            mToDoViewModel.deleteAll()
            Toast.makeText(requireContext(),"Successfully removed everything ", Toast.LENGTH_SHORT).show()

        }
        builder.setNegativeButton("No"){_,_ ->}
        builder.setTitle("Delete evertthing ?")
        builder.setMessage("Are you sure you want to delete everything ?")
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query!=null){
            searchThroughDatabase(query)
        }
        return true
    }

    private fun searchThroughDatabase(query: String) {
        val  searchQuery ="%$query%"


        mToDoViewModel.searchDatabase(searchQuery).observe(this, Observer {list->
            list?.let {
                adapter.setData(it)
            }
        })

    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query!=null){
            searchThroughDatabase(query)
        }
        return true
    }


}