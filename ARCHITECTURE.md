# Kiến trúc dự án Moon Page (Architecture)

Dự án Moon Page được xây dựng dựa trên **Clean Architecture** kết hợp với mô hình **MVVM** (Model - View - ViewModel) và sử dụng **Jetpack Compose** cho phần giao diện người dùng. Việc chia nhỏ dự án thành các module rõ ràng giúp code dễ bảo trì, dễ mở rộng và dễ dàng viết Unit Test.

---

## 1. Tổng quan về Clean Architecture & MVVM

- **Clean Architecture** giúp chia ứng dụng thành các lớp (layers) độc lập với nhau. Nguyên tắc quan trọng nhất là *Dependency Rule*: các lớp bên ngoài (UI, DB, Network) chỉ được phép phụ thuộc vào các lớp bên trong (Domain, Business Logic), không có chiều ngược lại.
- **MVVM** giúp tách biệt logic giao diện (View) khỏi logic nghiệp vụ (ViewModel), ViewModel sẽ tương tác với Domain layer để lấy dữ liệu và cung cấp State cho View hiển thị.

Dự án được chia thành 4 package (thư mục) chính nằm trong `app/src/main/java/com/diary/moonpage/`:
1. `core`
2. `data`
3. `domain`
4. `presentation`

---

## 2. Chi tiết từng Module / Layer

### 2.1. `core` (Lõi ứng dụng)
Chứa các thành phần, cấu hình và các class tiện ích (utilities) được sử dụng chung trên toàn bộ ứng dụng.
- **`di` (Dependency Injection):** Chứa các module của Dagger Hilt để cung cấp (provide) các instances tự động (Singleton) cho toàn app.
  - `DatabaseModule.kt`: Cung cấp instance của Room Database và các DAO.
  - `NetworkModule.kt`: Cung cấp Retrofit, OkHttpClient, Gson và các API interfaces.
  - `RepositoryModule.kt`: Gắn kết (bind) các interface (domain) với implementation thực tế (data).
  - `UseCaseModule.kt`: Cung cấp các UseCase.
- **`network`:** Các cấu hình mạng, ví dụ `AuthInterceptor.kt` (tự động gắn token vào header của mọi request).
- **`theme`:** Định nghĩa màu sắc (`Color.kt`), typography (`Type.kt`), và `Theme.kt` cho Jetpack Compose.
- **`util`:** Chứa các class tiện ích và quản lý lưu trữ cục bộ (DataStore/SharedPreferences).
  - `TokenManager.kt`, `SettingsPreferencesManager.kt`, `UserManager.kt`.
  - Các hàm helper: `DateUtils.kt`, `ImageUtils.kt`.

### 2.2. `data` (Lớp Dữ liệu)
Chịu trách nhiệm tương tác trực tiếp với cơ sở dữ liệu nội bộ (Local) và máy chủ (Remote API), sau đó chuyển đổi dữ liệu thô thành định dạng mà ứng dụng có thể hiểu được.
- **`local` (Cơ sở dữ liệu Room):**
  - `MoonPageDatabase.kt`: Định nghĩa SQLite database.
  - `dao/`: Data Access Objects - Chứa các hàm truy vấn SQL (insert, delete, query).
  - `entity/`: Định nghĩa cấu trúc các bảng trong DB.
  - `Converters.kt`: Chuyển đổi dữ liệu phức tạp (như List, Date) để lưu vào DB.
- **`remote` (API Retrofit):**
  - `api/`: Các interface định nghĩa endpoint gọi API (ví dụ: `AuthApi.kt`, `DailyLogApi.kt`).
  - `dto/` (Data Transfer Objects): Các data class tương ứng với cấu trúc JSON trả về từ API.
- **`repository` (Repository Implementations):**
  - Chứa các class thực thi (`*Impl.kt`) cho các interface định nghĩa ở lớp `domain`. Lớp này xử lý logic "Khi nào lấy từ Local, khi nào lấy từ mạng". Ví dụ: `AuthRepositoryImpl.kt`.

### 2.3. `domain` (Lớp Nghiệp vụ cốt lõi)
Đây là "trái tim" của ứng dụng, chứa các quy tắc nghiệp vụ (business rules). Lớp này **hoàn toàn độc lập**, không phụ thuộc vào `data` hay `presentation`, không biết gì về UI, Android SDK hay Database.
- **`model`:** Các data class đại diện cho các đối tượng cốt lõi (`User.kt`, `DailyLog.kt`). Đây là model thuần túy, không chứa các annotation của Room hay Retrofit.
- **`repository`:** Chứa các **Interfaces** repository. Lớp `domain` chỉ nói "Tôi cần những hàm lấy dữ liệu này", còn việc lấy ra sao là việc của lớp `data`.
- **`usecase` (Interactors):** Mỗi UseCase đại diện cho **một hành động duy nhất** của người dùng hoặc hệ thống.
  - `auth/`: `LoginUseCase`, `RegisterUseCase`...
  - `validation/`: Kiểm tra tính hợp lệ dữ liệu đầu vào (`ValidateEmail`, `ValidatePassword`).
  - Việc chia nhỏ UseCase giúp code dễ đọc, dễ tái sử dụng và dễ viết Unit Test.

### 2.4. `presentation` (Lớp Giao diện - UI)
Lớp này chịu trách nhiệm hiển thị giao diện (bằng Jetpack Compose) và nhận các sự kiện tương tác từ người dùng.
- **`components`:** Các UI Widget nhỏ, có thể tái sử dụng ở nhiều nơi (Nút bấm, TopBar, Item danh sách).
- **`navigation`:** `AppNavigation.kt` định nghĩa tất cả các màn hình (Screen) và logic chuyển trang (NavHost).
- **`screens`:** Chứa UI (Giao diện) và ViewModel, được chia thành các thư mục theo tính năng (Feature-based):
  - `auth/`: Đăng nhập, Đăng ký, `LoadingScreen`, `AuthViewModel`.
  - `calendar/`: Màn hình lịch, `DailyLogScreen`, chọn nhạc (`MusicScreen`).
  - `moment/`: Camera, chi tiết khoảnh khắc.
  - `profile/`: Cài đặt (`SettingsScreen`), thông tin tài khoản.
  - `store/`, `stats/`...
  - *Mối quan hệ:* **View** (Compose) gửi sự kiện cho **ViewModel**. ViewModel sẽ gọi các **UseCase** để lấy/lưu dữ liệu, sau đó cập nhật kết quả vào `StateFlow`. View sẽ tự động vẽ lại (recompose) khi `StateFlow` thay đổi.

### 2.5. Các thành phần quan trọng khác
- **`MainActivity.kt`:** Điểm bắt đầu (Entry point) của ứng dụng. Khởi tạo giao diện Compose và xử lý các Deeplink (như callback của Spotify).
- **`MoonPageApplication.kt`:** Lớp Application, được gắn `@HiltAndroidApp` để kích hoạt cơ chế Dependency Injection của Hilt.
- **`service/MoonFirebaseMessagingService.kt`:** Dịch vụ chạy ngầm để nhận thông báo (Push Notifications) từ Firebase.

---

## 3. Luồng dữ liệu hoạt động (Data Flow)

Hãy xem xét một luồng ví dụ khi người dùng nhấn nút **"Đăng nhập"**:

1. **UI (`LoginScreen`)**: Bắt sự kiện click và gọi hàm `login()` trong **`AuthViewModel`**.
2. **`AuthViewModel`**: Gọi **`LoginUseCase`** (nằm ở lớp `domain`) và truyền email, mật khẩu vào.
3. **`LoginUseCase`**: Gọi interface **`AuthRepository`** (cũng nằm ở `domain`).
4. **`AuthRepositoryImpl`** (thuộc lớp `data`): Thực thi interface này. Nó sử dụng **`AuthApi`** (Retrofit) để gửi request lên server.
5. **Server**: Trả về một **DTO** (Data Transfer Object).
6. **`AuthRepositoryImpl`**: Chuyển (Map) DTO thành **Domain Model** (`User.kt`) và trả về cho UseCase.
7. **`LoginUseCase`**: Xử lý logic thêm nếu có, rồi trả kết quả về cho ViewModel.
8. **`AuthViewModel`**: Cập nhật `UiState` (ví dụ: chuyển trạng thái loading = false, isSuccess = true).
9. **UI (`LoginScreen`)**: Vì đang "lắng nghe" `UiState` (bằng `collectAsState`), giao diện sẽ tự động chuyển sang màn hình trang chủ (Calendar).

---

Sự phân tách này giúp Moon Page là một dự án "sạch", dễ dàng debug, bảo trì và rất thuận lợi khi thêm các lập trình viên mới vào dự án.
