package com.example.kotlinactivities.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.google.firebase.database.*

class ViewUsersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var usersList: ArrayList<User>
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_users, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        usersList = ArrayList()
        userAdapter = UserAdapter(usersList)
        recyclerView.adapter = userAdapter
        database = FirebaseDatabase.getInstance().getReference("users")
        fetchUsers()
        return view
    }

    private fun fetchUsers() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        user?.let { usersList.add(it) }
                    }
                    userAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "No users found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load users: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // RecyclerView Adapter for displaying users
    class UserAdapter(private val usersList: ArrayList<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
        class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
            val emailTextView: TextView = itemView.findViewById(R.id.userEmailTextView)
            val phoneTextView: TextView = itemView.findViewById(R.id.userPhoneTextView)
            val roleTextView: TextView = itemView.findViewById(R.id.userRoleTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
            return UserViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            val user = usersList[position]
            holder.nameTextView.text = user.name
            holder.emailTextView.text = user.email
            holder.phoneTextView.text = user.phoneNumber
            holder.roleTextView.text = user.role
        }

        override fun getItemCount(): Int = usersList.size
    }

    // Data class for user
    data class User(
        val name: String = "",
        val email: String = "",
        val phoneNumber: String = "",
        val role: String = ""
    )
}
