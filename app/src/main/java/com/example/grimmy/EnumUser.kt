package com.example.grimmy

enum class EnumStatus(val statusName: String) {
    ELEMENT("초등학생"),       // 초등학생
    MIDDLE("중학생"),         // 중학생
    HIGH("고등학생"),             // 고등학생
    UNIVERSITY("대학생"), // 대학생
    ADULT("성인"),           // 성인
    GAPYEAR("N수생"),       // 갭이어
    QUALIFICATION("검정고시"); // 검정고시
}

enum class EnumExamType(val examType: String) {
    DESIGN("기초디자인"),
    WATERCOLOR("수채화"),
    CHANGE("사고의 전환"),
    WESTERN("서양화"),
    ORIENTAL("동양화"),
    MOCK("조소"),
    EXPRESSION("발상과 표현"),
    BASIC("기초소양"),
    DRAWING("소묘"),
    ANIMATION("애니메이션"),
    PORTFOLIO("포트폴리오"),
    INTERVIEW("면접");
}