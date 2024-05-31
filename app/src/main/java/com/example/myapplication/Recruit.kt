package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Switch
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase

/*
TODO: 모집글 검색 구현
 */

class Recruit : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recruitAdapter: RecruitAdapter
    private val firestore = FirebaseFirestore.getInstance()
    // startactivity 대체
    private lateinit var postRecruitmentLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

        // 창 닫기 리스너
        postRecruitmentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.i("intent 닫힘", "intent 닫힘.")
                firestore.collection("recruitment")
                    .orderBy("created_at", Query.Direction.DESCENDING) // 최신순으로 가져오기
                    .limit(8)
                    .get()
                    .addOnSuccessListener { documents ->
                        val recruitList = documents.toObjects(RecruitDataModel::class.java)
                        recruitAdapter.updateList(recruitList)
                    }
                    .addOnFailureListener { exception ->
                        Log.w("MainActivity", "Error getting documents: ", exception)
                    }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recruit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // recyclerView 설정
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recruitAdapter = context?.let { RecruitAdapter(emptyList(), it) }!!
        recyclerView.adapter = recruitAdapter

        // "작성하기" 버튼 클릭 리스너
        val btPostNew = view.findViewById<ImageButton>(R.id.ibt_PostNew)
        btPostNew.setOnClickListener {
            val intent = Intent(activity, PostRecruitment::class.java)
            postRecruitmentLauncher.launch(intent)
        }

        // 필터링
        val switch: SwitchCompat = view.findViewById(R.id.switch_Filter)
        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                filterRecruitData()
            }
            else {
                loadRecruitData()
            }
        }


        // 검색하기
        val search = view.findViewById<SearchView>(R.id.sv_SearchBox)
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchRecruitData(query)
                } else {
                    loadRecruitData()
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        // Firestore에서 데이터 가져오기
        loadRecruitData()

    }
    private fun searchRecruitData(query: String) {
        firestore.collection("recruitment")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .limit(50)  // Increase limit if necessary to ensure enough results
            .get()
            .addOnSuccessListener { documents ->
                val recruitList = documents.toObjects(RecruitDataModel::class.java)
                    .filter { document ->
                        val titleContent = (document.title + " " + document.place + " " + document.content).lowercase()
                        query.lowercase() in titleContent
                    }
                recruitAdapter.updateList(recruitList)
            }
            .addOnFailureListener { exception ->
                Log.w("Recruit", "Error getting documents: ", exception)
            }
    }
    fun refreshRecyclerView() {
        loadRecruitData()
    }

    private fun loadRecruitData() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        firestore.collection("recruitment")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .limit(12)
            .get()
            .addOnSuccessListener { documents ->
                val recruitList = documents.toObjects(RecruitDataModel::class.java)
                recruitAdapter.updateList(recruitList)
            }
            .addOnFailureListener { exception ->
                Log.w("Recruit", "Error getting documents: ", exception)
            }
    }

    private fun filterRecruitData() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        var userAge: Long = -1
        var userMajor: String = ""
        var userSex: String = ""
        if (user != null) {
            firestore.collection("user").document(user.uid).get().addOnSuccessListener {document->
                userAge = document.getLong("age")?:-1
                userMajor = document.getString("major")?:""
                userSex = document.getString("sex")?:""
            }
        }


        firestore.collection("recruitment")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .limit(12)
            .get()
            .addOnSuccessListener { documents ->
                val recruitList = documents.toObjects(RecruitDataModel::class.java)
                    .filter {document->
                        // 논리 조건식 필터링
                        // 자신이 올린 글이거나
                        (document.uploader_id == (user?.uid?:""))
                                ||
                                // 조건을 만족하는 글만 보이기
                                ((document.keyword_age_max == -1 || document.keyword_age_max >= userAge) && (document.keyword_age_min == -1 || document.keyword_age_min <= userAge)
                                && (document.keyword_sex == "" || document.keyword_sex == userSex)
                                && (document.keyword_major == "" || document.keyword_major == userMajor))
                    }
                recruitAdapter.updateList(recruitList)
            }
            .addOnFailureListener { exception ->
                Log.w("Recruit", "Error getting documents: ", exception)
            }
    }

}