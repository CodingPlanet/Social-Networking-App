package com.android.messenger.Fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.android.messenger.Activites.NewChat;
import com.android.messenger.Adapter.DialogLoadingAdapter;
import com.android.messenger.Adapter.InboxAdapter;
import com.android.messenger.Helpers.CurrentUser;
import com.android.messenger.Helpers.DatabaseRef;
import com.android.messenger.Model.ChatInfo;
import com.android.messenger.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatFragment extends Fragment {


    TextView txtusername;
    CircleImageView profileimageview;
    EditText edtSearch;
    Button btnStartNewChat;
    Context context;
    RecyclerView recyclerViewRecentChat;
    List<String> chatIdList  = new ArrayList<String>();
    int counter =1;
    List<ChatInfo> chatInfoList  = new ArrayList<ChatInfo>();
    LinearLayout layout_noResult;

    DialogLoadingAdapter dialogLoadingAdapter;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        init(view);

        dialogLoadingAdapter = new DialogLoadingAdapter(context);
        dialogLoadingAdapter.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogLoadingAdapter.setCancelable(false);
        dialogLoadingAdapter.setCanceledOnTouchOutside(false);
        dialogLoadingAdapter.show();



        recyclerViewRecentChat.setLayoutManager(new LinearLayoutManager(context));


        DatabaseRef.dbref_inbox.child(CurrentUser.userInfo.uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final long countAllChats = dataSnapshot.getChildrenCount();
                    Log.d("chatId","countAllChats: "+countAllChats);
                    counter = 1;

                    if(countAllChats == 0){
                        dialogLoadingAdapter.dismiss();
                    }

                chatIdList.clear();

                    for(final DataSnapshot dataSnapshot_individualChat : dataSnapshot.getChildren()){

                        chatInfoList.clear();

                        String chatId = String.valueOf(dataSnapshot_individualChat.getKey());
                        chatIdList.add(chatId);
                        Log.d("chatId","chatId: "+chatId);

                        if(counter == countAllChats){

//                            int counter2 = 1;
//                            int countAllChats2 = chatIdList.size();

                            for(String chatid : chatIdList){

                                Log.d("chatId",chatId);
                               // dialogLoadingAdapter.dismiss();

                                DatabaseRef.dbref_chatinfo.child(chatid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        ChatInfo chatInfo = dataSnapshot.getValue(ChatInfo.class);
                                        Log.d("chatId",chatInfo.lastMsg);
                                        chatInfoList.add(chatInfo);


                                            InboxAdapter inboxAdapter = new InboxAdapter(context,chatInfoList);
                                            recyclerViewRecentChat.setAdapter(inboxAdapter);
                                            inboxAdapter.notifyDataSetChanged();
                                            dialogLoadingAdapter.dismiss();

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        }

                        counter++;
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        txtusername.setText(CurrentUser.userInfo.username);

        if(!CurrentUser.userInfo.profileImageUrl.equals("default")) {
            Glide.with(context).load(CurrentUser.userInfo.profileImageUrl).placeholder(R.drawable.default_user_img).into(profileimageview);
        }






        btnStartNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent_newchat = new Intent(context, NewChat.class);
                startActivity(intent_newchat);
            }
        });


        return view;
    }

    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }




    public void init(View view){

        txtusername = (TextView)view.findViewById(R.id.txtusername);
        profileimageview = (CircleImageView)view.findViewById(R.id.profileimageview);
        edtSearch = (EditText)view.findViewById(R.id.edtSearch);
        btnStartNewChat = (Button)view.findViewById(R.id.btnStartNewChat);
        recyclerViewRecentChat = (RecyclerView)view.findViewById(R.id.recyclerViewRecentChat);
        layout_noResult = (LinearLayout)view.findViewById(R.id.layout_noResult);

        context = getActivity();

    }

}
