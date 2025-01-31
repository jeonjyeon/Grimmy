package com.example.grimmy.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grimmy.User


class UserViewModel : ViewModel() {
    private var _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

//    private val _nickname = MutableLiveData<String>()
//    val nickname: LiveData<String> get() = _nickname
//
//    fun setNickname(nickname: String) {
//        _nickname.value = nickname
//    }

    // 사용자 정보 설정
    fun setUser(nickname: String?, birthYear: String?, studentStatus: String?, examType: String?) {
        val currentUser = _user.value ?: User("", "", "", "")
        _user.value = currentUser.copy(
            nickname = nickname ?: currentUser.nickname,
            birthYear = birthYear ?: currentUser.birthYear,
            studentStatus = studentStatus ?: currentUser.studentStatus,
            examType = examType ?: currentUser.examType
        )// 이전 코드
        Log.d("UserViewModel", "User updated: $_user.value")
    }

    // 개별 정보 설정
    fun setNickname(nickname: String) {
        setUser(nickname, null, null, null)
        Log.d("UserViewModel", "닉네임이 설정되었습니다: $nickname") // 로그 출력

    }

    fun setBirthYear(birthYear: String) {
        setUser(null, birthYear, null, null)
        Log.d("UserViewModel", "출생년도가 설정되었습니다: $birthYear") // 로그 출력

    }

    fun setStudentStatus(studentStatus: String) {
        setUser(null, null, studentStatus, null)
        Log.d("UserViewModel", "신분이 설정되었습니다: $studentStatus") // 로그 출력

    }

    fun setExamType(examType: String) {
        setUser(null, null, null, examType)
        Log.d("UserViewModel", "입시 유형이 설정되었습니다: $examType") // 로그 출력

    }

    // 서버에서 사용자 데이터 가져오는 메소드
    fun fetchNicknameFromServer() {
        // API 호출 로직을 여기에 추가하여 서버에서 닉네임을 가져오고 _nickname을 업데이트
    }

    // 서버에 사용자 데이터 저장하는 메소드
    fun saveNicknameToServer(nickname: String) {
        // API 호출 로직을 여기에 추가하여 서버에 닉네임을 저장
    }
}
