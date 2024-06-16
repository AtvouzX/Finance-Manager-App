package com.company.financemanager.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.financemanager.R
import com.company.financemanager.adapter.HomeAdapter
import com.company.financemanager.databinding.FragmentHomeBinding
import com.company.financemanager.models.HomeHistory
import com.google.firebase.database.DatabaseReference

private lateinit var homeViewModel: HomeViewModel
private lateinit var HomeRecyclerview : RecyclerView
private lateinit var adapter : HomeAdapter
class HomeFragment : Fragment() {


    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        HomeRecyclerview = view.findViewById(R.id.tr_history)
        HomeRecyclerview.layoutManager = LinearLayoutManager(context)
        HomeRecyclerview.setHasFixedSize(true)
        adapter = HomeAdapter()
        HomeRecyclerview.adapter = adapter

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        homeViewModel.history.observe(viewLifecycleOwner, Observer {
            adapter.updateHomeList(it)
        })
    }


}