package com.boss.qna.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boss.qna.Models.Answer;
import com.boss.qna.R;

import java.util.ArrayList;

public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.AnswerHolder> {
    ArrayList<Answer> answerArrayList;

    public AnswerAdapter(ArrayList<Answer> answers) {
        this.answerArrayList = answers;
    }

    @NonNull
    @Override
    public AnswerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AnswerHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.answer_dialog_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerHolder holder, int position) {
        Answer answer = answerArrayList.get(position);
        holder.textView.setText(answer.ans);
    }

    @Override
    public int getItemCount() {
        return answerArrayList.size();
    }

    public class AnswerHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public AnswerHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }
}
