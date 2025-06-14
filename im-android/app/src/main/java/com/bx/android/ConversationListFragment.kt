package com.bx.android

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bx.android.model.MessageStore
import com.bx.android.ConversationAdapter

class ConversationListFragment : Fragment(R.layout.fragment_list) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerView)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = ConversationAdapter(MessageStore.getConversations())
    }
}
