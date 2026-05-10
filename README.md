# Moon Page - Ứng dụng Nhật ký Mặt trăng

Moon Page là một ứng dụng nhật ký cá nhân hiện đại, được xây dựng bằng Jetpack Compose và Kotlin, giúp bạn ghi lại những khoảnh khắc, cảm xúc và hoạt động hàng ngày một cách tinh tế và bảo mật.

## ✨ Tính năng nổi bật

- **Ghi nhật ký đa phương tiện:** Lưu lại cảm xúc, hoạt động và hình ảnh mỗi ngày.
- **Đa ngôn ngữ:** Hỗ trợ đầy đủ tiếng Việt, tiếng Anh và tiếng Pháp.
- **Bảo mật tối đa:** Hỗ trợ khóa bằng mã PIN (Passcode) và xác thực vân tay (Biometric).
- **Tùy biến giao diện:** Thay đổi chủ đề (Theme) và chế độ tối (Dark Mode) linh hoạt.
- **Thống kê:** Theo dõi sự thay đổi tâm trạng và thói quen qua các biểu đồ trực quan.
- **Tích hợp Spotify:** Kết nối với âm nhạc bạn yêu thích ngay trong ứng dụng.
- **Đồng bộ hóa:** Hỗ trợ thông báo qua Firebase và lưu trữ dữ liệu an toàn.

## 🛠 Công nghệ sử dụng

- **Ngôn ngữ:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Kiến trúc:** MVVM (Model-View-ViewModel) + Clean Architecture
- **Dependency Injection:** Dagger Hilt
- **Lưu trữ dữ liệu:** Room Database (Local) & DataStore (Preferences)
- **Mạng:** Retrofit & OkHttp
- **Khác:** Firebase Messaging, Biometric API, Health Connect, Coil (Load ảnh).

## 🚀 Cách cài đặt và Chạy ứng dụng

### 1. Yêu cầu hệ thống
- **Android Studio:** Phiên bản Ladybug (2024.2.1) hoặc mới hơn.
- **JDK:** Java 17 hoặc cao hơn.
- **Android SDK:** API 26 (Android 8.0) trở lên.

### 2. Tải mã nguồn
Bạn có thể tải mã nguồn bằng cách sử dụng git:
```bash
git clone <url-repository-cua-ban>
```

### 3. Mở và Chạy Project
1. Mở **Android Studio**.
2. Chọn **File > Open** và tìm đến thư mục `DiaryApp`.
3. Đợi Android Studio hoàn tất việc đồng bộ Gradle (Gradle Sync).
4. Kết nối thiết bị Android thật hoặc khởi động trình giả lập (Emulator).
5. Nhấn nút **Run (Tam giác xanh)** hoặc phím tắt `Shift + F10` để khởi động ứng dụng.

## 📁 Cấu trúc thư mục chính

- `app/src/main/java/com/diary/moonpage/core`: Chứa các tiện ích, chủ đề (theme) và cấu hình dùng chung.
- `app/src/main/java/com/diary/moonpage/data`: Xử lý dữ liệu (Local Room, Remote API, Repository Implementation).
- `app/src/main/java/com/diary/moonpage/domain`: Chứa logic nghiệp vụ (Models, Interfaces, UseCases).
- `app/src/main/java/com/diary/moonpage/presentation`: Thành phần UI (Screens, ViewModels, Components).
- `app/src/main/res`: Chứa tài nguyên (Strings đa ngôn ngữ, Images, Layouts).

## 📝 Thông tin thêm
Ứng dụng đang trong quá trình hoàn thiện và liên tục được cập nhật thêm các tính năng mới để mang lại trải nghiệm tốt nhất cho người dùng.

---