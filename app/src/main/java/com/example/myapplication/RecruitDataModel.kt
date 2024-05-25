package com.example.myapplication

import com.google.firebase.Timestamp

class RecruitDataModel {
    val uploader_id: String = ""
    val post_id: String = ""
    var keyword_age_max: Int = -1
    var keyword_age_min: Int = -1
    var keyword_major: String = ""
    var keyword_mbti: String = ""
    var keyword_sex: String = ""
    var headcount_current: Int = 1
    var headcount_max: Int = 2
    var title: String = ""
    var uploader: String = ""
    var place: String = ""
    var created_at: Timestamp? = null

}