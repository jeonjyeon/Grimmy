# 그리미 Grimmy🎨

## 1. Code 컨벤션
### **Layout**
- activity_ : Activity에 사용되는 layout
- fragment_ : Fragment에 사용되는 layout
- dialog_ : Dialog에 사용되눈 layout

**예시**
- activity_main : Main화면에 사용될 Acitivty
- fragment_home : Home화면에 사용될 Fragment
- dialog_Loading : Loading 중일 때 사용될 Dialog

### **ID**
- (layout명)_(사용 목적)_tv : TextView
- (layout명)_(사용 목적)_iv : ImageView
- (layout명)_(사용 목적)_rv : RecyclerView
- (layout명)_(사용 목적)_et : EditText
- (layout명)_(사용 목적)_pb : ProgressBar
- (layout명)_(사용 목적)_fl : FrameLayout
- (layout명)_(사용 목적)_ci : CircleIndicator

※ Button, ImageButton은 사용하지 않고, TextView, ImageView 사용.

**예시**
- login_login_btn_iv : Login화면에 login버튼으로 사용될 ImageView
- book_title_tv : Book화면에 제목으로 사용될 TextView
- Singup_email_et : Singup화면에서 이메일을 입력할 EditText

### **Drawable**
- btn_ : 버튼으로 사용될 이미지
- ic_ : 아이콘으로 사용될 이미지
- bg_ : 배경으로 사용될 이미지
- img_ : 아이콘 또는 버튼이 아닌 화면에 띄워질 이미지

※ Figma에서 가져온 Drawable 파일 추가 시 svg형태로 불러오기.

※ bg의 경우 모양, 색깔, 속성(굴곡, 윤곽선 굵기 등) 순으로 명명.

**예시**
- bg_rect_black_radius_30.xml
- btn_play
- ic_main
- img_banner

+ 필요한 라이브러리는 추가 후 추가 목적 주석으로 작성.


## 2. Branch 전략
- 개발자별로 Branch 이름 정함.


## 3. Commit 컨벤션
- Commit 메시지에 작성 파일과 구현 화면 or 기능 상세 설명 작성

**예시**
[activity_login] 로그인 화면 구현
[LoginActivity.kt] 로그인 기능 구현


## 4. Android Studio 환경 설정
### **Android Studio 버전**
- Ladybug

### **targetSDK, minSDK 버전**
- targetSDK: 34
- minSDK: 24

### **테스트 방식**
- 처음 화면 구성 및 기초 기능 구현 시 IDE 내 Emulator로 테스트
- api 연동 후, 어느정도 앱이 완성되었을 때 실제 디바이스로 테스트트
