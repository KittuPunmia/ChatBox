package com.kittu.chatboxfirebase;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private Context context;
    private List<messages> message;
FirebaseUser user;
    MessageAdapter(Context context, List<messages> message) {
        this.context = context;
        this.message = message;
    }
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.message_single, parent, false);
        return new MessageAdapter.MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        user=FirebaseAuth.getInstance().getCurrentUser();
        messages c=message.get(position);
        String messagetype=c.getType();
        String from_user=c.getFrom();
         if(from_user!=null) {
          if (from_user.equals(user.getUid())) {
        holder.messageText.setBackgroundColor(Color.WHITE);
        holder.messageText.setTextColor(Color.BLACK);
       // Picasso.get().load(c.getThumb_image()).placeholder(R.drawable.profile).fit().centerCrop().into(holder.profileImage);

    } else {
        holder.messageText.setBackgroundResource(R.drawable.message_text_background);
        holder.messageText.setTextColor(Color.WHITE);
    }
     /*   if(!c.getImage().equals("default")) {
            Picasso.get().load(c.getThumb_image()).placeholder(R.drawable.profile).fit().centerCrop().into(holder.user_single_image);
        }*/
}

if(messagetype.equals("text"))
{
  holder.messageText.setText(c.getMessage());
  holder.message_image_layout.setVisibility(View.INVISIBLE);

}
else
{
    holder.messageText.setVisibility(View.INVISIBLE);
    Picasso.get().load(c.getMessage()).centerCrop().into(holder.message_image_layout);

}
    }

    @Override
    public int getItemCount() {
        return message.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        CircleImageView profileImage;
        ImageView message_image_layout;
        TextView name_text_layout;

        public MessageViewHolder(View itemView) {
            super(itemView);
                messageText=itemView.findViewById(R.id.message_text_layout);
                profileImage=itemView.findViewById(R.id.message_profile_layout);
                message_image_layout=itemView.findViewById(R.id.message_image_layout);
                name_text_layout=itemView.findViewById(R.id.name_text_layout);
            }
    }
}
