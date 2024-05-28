package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Chat : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var chatRoomAdapter: ChatRoomAdapter
    private val chatRooms = mutableListOf<ChatRoom>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.rcv_ChatList)
        chatRoomAdapter = ChatRoomAdapter(chatRooms) { chatRoom ->
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("roomId", chatRoom.roomId)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = chatRoomAdapter

        database = FirebaseDatabase.getInstance().reference.child("chatRooms")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatRooms.clear()
                for (data in snapshot.children) {
                    val chatRoom = data.getValue(ChatRoom::class.java)
                    if (chatRoom != null && chatRoom.users.containsKey(currentUser.uid)) {
                        chatRooms.add(chatRoom)
                    }
                }
                chatRoomAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

    }
}