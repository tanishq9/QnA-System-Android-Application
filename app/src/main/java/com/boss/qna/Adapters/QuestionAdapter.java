package com.boss.qna.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boss.qna.Models.Question;
import com.boss.qna.R;

import java.util.ArrayList;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionHolder> {
    ArrayList<Question> questionArrayList = new ArrayList<>();
    Context context;

    public QuestionAdapter(ArrayList<Question> questions, Context icontext) {
        this.questionArrayList = questions;
        this.context = icontext;
    }

    @NonNull
    @Override
    public QuestionAdapter.QuestionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new QuestionHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionAdapter.QuestionHolder holder, int position) {
        Question question = questionArrayList.get(position);
        holder.textView.setText(question.ques);
    }

    @Override
    public int getItemCount() {
        return questionArrayList.size();
    }

    public class QuestionHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public QuestionHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.question);
        }
    }
}
