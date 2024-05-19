package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        ArrayList<String> list = new ArrayList<>();
        list.add("간호학과");
        list.add("건축학과");
        list.add("경영학과");
        list.add("글로벌한국학과");
        list.add("동물자원과학과");
        list.add("물리치료학과");
        list.add("바이오융합공학과");
        list.add("보건관리학과");
        list.add("빅데이터클라우드공학과");
        list.add("사회복지학과");
        list.add("상담심리학과");
        list.add("스미스학부");
        list.add("식품영양학과");
        list.add("신학과");
        list.add("아트앤디자인학과");
        list.add("약학과");
        list.add("영어영문학과");
        list.add("유아교육과");
        list.add("음악학과");
        list.add("인공지능융합학부");
        list.add("체육학과");
        list.add("컴퓨터공학부");
        list.add("항공관광외국어학부");
        list.add("화학생명과학과");
        //for (int i = 25; i < 100; i++) {
        //    list.add(String.format("TEXT %d", i));
       // }

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        RecyclerView recyclerView = findViewById(R.id.recycler1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 리사이클러뷰에 SimpleTextAdapter 객체 지정.
        SimpleTextAdapter adapter = new SimpleTextAdapter(list);
        recyclerView.setAdapter(adapter);
    }
}