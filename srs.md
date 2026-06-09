# ĐẶC TẢ YÊU CẦU PHẦN MỀM (SRS)

## Ứng dụng di động Nhật ký Cảm xúc Cá nhân - Moon Page

| Phiên bản | 1.0 — Draft |
| :--- | :--- |
| **Ngày lập** | 04/06/2026 |
| **Đơn vị phụ trách** | Nhóm Phát triển Moon Page |
| **Trạng thái** | Chờ xác nhận — Bản đặc tả yêu cầu chi tiết hệ thống |

---

## 1. GIỚI THIỆU

### 1.1 Mục đích
Tài liệu này mô tả chi tiết các yêu cầu chức năng, phi chức năng, kịch bản ca sử dụng (usecase) chi tiết và cấu trúc cơ sở dữ liệu (cục bộ Room và API máy chủ) cho ứng dụng di động Android **Moon Page - Personal Mood Diary**. Tài liệu làm cơ sở kỹ thuật thống nhất cho quá trình thiết kế giao diện, lập trình, kiểm thử và nghiệm thu sản phẩm giữa các bên liên quan.

### 1.2 Phạm vi hệ thống
Hệ thống bao gồm ứng dụng di động Android (Client) sử dụng Clean Architecture, MVVM kết hợp với Jetpack Compose, và máy chủ Backend (Server API) để xử lý dữ liệu đồng bộ. Ứng dụng hỗ trợ người dùng theo dõi và chăm sóc sức khỏe tinh thần thông qua việc ghi chép nhật ký hàng ngày, phân tích số liệu thống kê, và trải nghiệm cá nhân hóa giao diện.

> [!IMPORTANT]
> Ứng dụng di động Moon Page được thiết kế độc quyền dành cho **Người dùng phổ thông (AT1)**. Mọi chức năng quản lý hệ thống của **Quản trị viên (AT2)** chỉ hoạt động thông qua các API Backend và cổng thông tin quản trị Web (Web Admin Portal) độc lập, hoàn toàn không xuất hiện và không thể truy cập từ ứng dụng di động Android.

| Trong phạm vi (In-Scope) | Mô tả |
| :--- | :--- |
| **Quản lý tài khoản & Bảo mật** | Đăng ký, đăng nhập hệ thống (Email & Google SSO), xác thực OTP qua email, thiết lập mã PIN bảo mật và vân tay (Biometrics) để khóa ứng dụng di động cục bộ. |
| **Ghi chép Nhật ký & Lịch** | Ghi nhật ký ngày với cảm xúc 5 mức độ (Hạt trăng - Cute Beans), thẻ hoạt động, ghi chú, ảnh chụp, thông tin giấc ngủ, chu kỳ kinh nguyệt. Tích hợp Health Connect (bước chân, calo, khoảng cách), Spotify (bài hát nghe kèm), và tự động nhận dạng thời tiết/nhiệt độ. |
| **Khoảnh khắc & Bảng tin** | Chụp/tải lên hình ảnh khoảnh khắc (Moments) sử dụng CameraX, thiết lập chế độ hiển thị công khai hoặc riêng tư trên dòng bảng tin mạng xã hội thu nhỏ của ứng dụng. |
| **Phân tích & Thống kê** | Cung cấp biểu đồ phân bổ cảm xúc, dòng chảy cảm xúc, năm trong hạt trăng (Year in Beans). Thống kê hoạt động tương quan tốt/tệ nhất, thói quen giấc ngủ, nhạc nghe nhiều nhất, và theo dõi chuỗi ngày viết nhật ký liên tiếp (Streak). |
| **Cửa hàng & Cá nhân hóa** | Cửa hàng giao diện (Theme Store) mua giao diện và bình Đóng băng chuỗi (Streak Freeze) bằng xu (Moon Coins). Trình thiết kế giao diện tùy chỉnh (Custom Theme Editor) hỗ trợ vẽ canvas, tùy chọn hình nền, và phối màu cho từng icon cảm xúc. |
| **Thông báo & Tiện ích** | Gửi thông báo đẩy qua Firebase Cloud Messaging (FCM). Hẹn giờ nhắc nhở ghi nhật ký hàng ngày (AlarmManager). Cung cấp 5 loại tiện ích (App Widgets) trên màn hình chính Android sử dụng Jetpack Glance. |
| **Quản trị hệ thống (Admin)** *Chỉ trên Web/Backend* | Trang quản trị quản lý danh sách người dùng (khóa/xóa), quản lý kho thẻ hoạt động chung, tải lên và cấu hình giao diện cửa hàng. |

### 1.3 Từ viết tắt
| Từ viết tắt | Ý nghĩa |
| :--- | :--- |
| **API** | Application Programming Interface (Giao diện lập trình ứng dụng) |
| **SSO** | Single Sign-On (Đăng nhập một chạm - ví dụ Google Sign-In) |
| **OTP** | One-Time Password (Mật khẩu sử dụng một lần) |
| **FCM** | Firebase Cloud Messaging (Dịch vụ thông báo đẩy của Firebase) |
| **PIN** | Personal Identification Number (Mã số định danh cá nhân / Mã bảo mật) |
| **DAO** | Data Access Object (Đối tượng truy cập dữ liệu trong SQLite Room) |
| **DTO** | Data Transfer Object (Đối tượng chuyển đổi dữ liệu khi tương tác với API) |
| **MVVM** | Model - View - ViewModel (Kiến trúc trình diễn ứng dụng) |
| **Widget** | Tiện ích nhanh hiển thị trên màn hình chính của hệ điều hành Android |

---

## 2. MÔ TẢ TỔNG QUAN HỆ THỐNG

### 2.1 Người dùng hệ thống (Actors)
| Vai trò | Mã | Trách nhiệm chính |
| :--- | :--- | :--- |
| **Người dùng phổ thông** | **AT1** | Đăng ký, đăng nhập tài khoản. Tạo lập nhật ký cảm xúc hàng ngày, tải lên hình ảnh khoảnh khắc cá nhân. Sử dụng xu tích lũy để mua sắm giao diện, thiết kế giao diện tùy chỉnh, và bảo vệ/nối lại chuỗi viết nhật ký (Streak). |
| **Quản trị viên** *(Tương tác qua Web/API)* | **AT2** | Truy cập các API quản trị để xem danh sách người dùng, thực hiện khóa (ban) hoặc xóa người dùng vi phạm. Quản lý danh mục hoạt động dùng chung và tải lên giao diện mới cho Cửa hàng từ trang Web quản lý. |
| **Hệ thống tự động** | **AT3** | Tự động kích hoạt báo thức AlarmManager nhắc nhở người dùng viết nhật ký hàng ngày. Tự động áp dụng bình đóng băng chuỗi khi người dùng bỏ lỡ ngày ghi chép. Cập nhật dữ liệu từ Health Connect/Spotify và cập nhật giao diện Glance Widgets. |

> [!IMPORTANT]
> Toàn bộ giao diện người dùng trên ứng dụng di động chỉ hiển thị các chức năng của **Tác nhân AT1 (Người dùng phổ thông)**. **Tác nhân AT2 (Quản trị viên)** không tương tác trên ứng dụng di động mà sử dụng trang Web quản lý Admin riêng để gọi các đầu API quản lý của Backend.

### 2.2 Giả định & Ràng buộc
- **Hệ điều hành**: Thiết bị cài đặt hệ điều hành Android với phiên bản tối thiểu là API 26 (Android 8.0 Oreo) để đáp ứng các tính năng bảo mật, Glance widgets và kết nối API hiện đại.
- **Kết nối mạng**: Ứng dụng yêu cầu kết nối mạng Internet để thực hiện đồng bộ hóa dữ liệu với Backend qua Retrofit API, nhận thông báo FCM và đăng tải Khoảnh khắc lên bảng tin cộng đồng. Khi ngoại tuyến (Offline), dữ liệu nhật ký vẫn được lưu trữ cục bộ trong cơ sở dữ liệu Room và sẽ tự động đồng bộ khi có mạng trở lại.
- **Quyền hạn thiết bị**: Người dùng cần cấp quyền truy cập Camera (CameraX), Thư viện ảnh (Photo Gallery), Dịch vụ vị trí (để tự động lấy thông tin thời tiết), quyền thông báo (FCM) và quyền đọc dữ liệu sức khỏe từ Health Connect.

---

## 3. YÊU CẦU CHỨC NĂNG

### Module 1: Quản lý Tài khoản & Bảo mật
| Mã | Chức năng | Actor |
| :--- | :--- | :--- |
| **QL-01** | Đăng ký tài khoản mới bằng Email + Tên người dùng + Mật khẩu. | AT1 |
| **QL-02** | Đăng nhập tài khoản bằng Email/Mật khẩu hoặc Đăng nhập một chạm thông qua Google (Google SSO). | AT1 |
| **QL-03** | Khôi phục mật khẩu: Gửi mã OTP 6 chữ số qua Email để đặt lại mật khẩu mới. | AT1 |
| **QL-04** | Cập nhật thông tin cá nhân (Tên hiển thị, giới tính, ngày sinh) và tải lên Ảnh đại diện (Avatar). | AT1 |
| **QL-05** | Thiết lập bảo mật cục bộ: Tạo/Xác minh mã khóa PIN (Passcode) và bật xác thực sinh trắc học vân tay (Biometrics) để khóa ứng dụng. | AT1 |
| **QL-06** | Xóa tài khoản vĩnh viễn: Thực hiện dọn dẹp dữ liệu trên Server và dọn dữ liệu cục bộ, yêu cầu xác thực bảo mật trước khi thực hiện. | AT1 |

### Module 2: Ghi chép Nhật ký & Lịch
| Mã | Chức năng | Actor |
| :--- | :--- | :--- |
| **NK-01** | Tạo mới/Cập nhật nhật ký ngày: Chọn cảm xúc hạt trăng (5 mức độ), viết ghi chú, chọn thẻ hoạt động (phân loại theo danh mục), thêm nhiều ảnh. | AT1 |
| **NK-02** | Ghi chép dữ liệu mở rộng: Nhập giờ ngủ (đi ngủ, thức dậy, thời gian ngủ), bật theo dõi chu kỳ kinh nguyệt (giai đoạn chu kỳ), đồng bộ chỉ số sức khỏe từ Health Connect (bước chân, calo, khoảng cách). | AT1, AT3 |
| **NK-03** | Tích hợp ngoại vi: Liên kết tài khoản Spotify để chọn bài hát đang nghe; tự động lấy thông tin thời tiết/nhiệt độ theo tọa độ GPS. | AT1, AT3 |
| **NK-04** | Xem, Lọc & Tìm kiếm nhật ký: Hiển thị nhật ký dạng danh sách hoặc cảm xúc theo tháng, lọc theo cảm xúc, hoạt động, giấc ngủ, chu kỳ, hoặc tìm kiếm ghi chú bằng từ khóa. | AT1 |
| **NK-05** | Chia sẻ hình ảnh: Xuất nhật ký ngày hoặc lịch tháng thành ảnh bitmap (tỷ lệ 1:1 hoặc 9:16) để lưu vào thiết bị hoặc chia sẻ lên mạng xã hội. | AT1 |

### Module 3: Khoảnh khắc
| Mã | Chức năng | Actor |
| :--- | :--- | :--- |
| **KK-01** | Đăng tải khoảnh khắc: Chụp ảnh trực tiếp từ trình Camera trong app hoặc chọn từ thư viện. | AT1 |
| **KK-02** | Xem khoảnh khắc cá nhân của riêng mình, có chức năng xem chi tiết thu phóng. | AT1 |
| **KK-03** | Chia sẻ khoảnh khắc, xóa khoảnh khắc vĩnh viễn khỏi hệ thống. | AT1 |

### Module 4: Phân tích & Thống kê
| Mã | Chức năng | Actor |
| :--- | :--- | :--- |
| **TK-01** | Báo cáo cảm xúc: Xem biểu đồ tròn phân bổ cảm xúc, biểu đồ đường thể hiện mood flow trong tháng và lưới xem tâm trạng cả năm. | AT1 |
| **TK-02** | Tương quan hoạt động: Hiển thị danh sách hoạt động có điểm cảm xúc trung bình tốt nhất và tệ nhất để chỉ ra mối liên hệ thói quen. | AT1 |
| **TK-03** | Thống kê sức khỏe & giấc ngủ: Biểu đồ theo dõi giấc ngủ hàng tháng, trung bình số giờ ngủ, biểu đồ phân tích bước chân, calo, khoảng cách đi bộ. | AT1 |
| **TK-04** | Thống kê âm nhạc: Danh sách các bài hát và nghệ sĩ được ghi nhật ký nhiều nhất trong tháng/năm. | AT1 |
| **TK-05** | Theo dõi chuỗi ghi chép (Streak): Đếm số ngày liên tục hoàn thành nhật ký, lưu giữ kỷ lục chuỗi dài nhất và tổng số ngày đã ghi nhận. | AT1, AT3 |

### Module 5: Cửa hàng & Cá nhân hóa
| Mã | Chức năng | Actor |
| :--- | :--- | :--- |
| **CH-01** | Duyệt & Mua giao diện: Sử dụng xu tích lũy (Moon Coins) để mở khóa các giao diện từ cửa hàng theo các bộ sưu tập. | AT1 |
| **CH-02** | Kích hoạt giao diện: Áp dụng giao diện đã sở hữu cho ứng dụng (thay đổi hình nền, màu chủ đạo hệ thống và màu/hình dáng của bộ 5 mood icon). | AT1 |
| **CH-03** | Mua & Sử dụng bình Đóng băng chuỗi (Streak Freeze): Dùng xu mua lượt đóng băng chuỗi và thực hiện khôi phục thủ công khi chuỗi viết nhật ký bị đứt đoạn. | AT1, AT3 |
| **CH-04** | Thiết kế giao diện tùy chỉnh (Custom Theme Editor): Hỗ trợ người dùng tự phối màu chủ đạo, chọn chế độ sáng/tối, chọn hình nền, vẽ trực tiếp bằng Canvas Brush (4 kiểu cọ, tùy chỉnh nét vẽ, tẩy và hoàn tác), và gán màu sắc riêng biệt cho từng hạt trăng cảm xúc. | AT1 |
| **CH-05** | Quản lý giao diện tùy chỉnh: Đổi tên hoặc chỉnh sửa lại các giao diện tự thiết kế. Slot tạo theme tùy chỉnh được mở khóa bằng xu (250 xu/slot). | AT1 |

### Module 6: Thông báo & Tiện ích
| Mã | Chức năng | Actor |
| :--- | :--- | :--- |
| **TB-01** | Nhắc nhở hàng ngày: Hẹn giờ nhắc nhở viết nhật ký cố định hàng ngày, tự động đăng ký lại báo thức khi thiết bị khởi động lại (Boot Aware). | AT1, AT3 |
| **TB-02** | Nhận thông báo hệ thống: Đọc thông báo nhắc nhở, thông báo chia sẻ khoảnh khắc, thông báo chúc mừng cột mốc chuỗi ngày (3 ngày, 7 ngày liên tiếp), thông báo về báo cáo tổng hợp dữ liệu cuối tháng/năm trong Hộp thư thông báo. | AT1, AT3 |
| **TB-03** | Tiện ích màn hình chính (Glance Widgets): Cấu hình và hiển thị 5 loại Widget trên màn hình chính: Nhập cảm xúc nhanh, Lịch cảm xúc tuần, Lịch cảm xúc tháng, Tóm tắt ngày và Tiện ích hiển thị khoảnh khắc ảnh. Tự động làm mới khi nhật ký thay đổi. | AT1, AT3 |

### Module 7: Quản trị hệ thống (Admin) *(Chỉ chạy trên Backend API & Portal quản trị Web)*
| Mã | Chức năng | Actor |
| :--- | :--- | :--- |
| **AD-01** | Quản lý người dùng: Xem danh sách, tìm kiếm người dùng theo tên/email, đổi trạng thái khóa tài khoản (Ban) đối với người dùng vi phạm điều khoản. | AT2 |
| **AD-02** | Quản lý hoạt động: Thêm hoạt động mới, đổi tên hoặc cập nhật icon hoạt động và danh mục hiển thị trên hệ thống. | AT2 |
| **AD-03** | Cấu hình cửa hàng giao diện: Tải lên hình ảnh, cấu hình tệp mô tả giao diện JSON (Theme Description Config) và thiết lập giá xu cho giao diện mới. | AT2 |

---

## 4. USE CASE CHI TIẾT

### UC-QL-01: Đăng ký tài khoản mới (Email & Mật khẩu)
- **Actor**: Người dùng phổ thông (AT1)
- **Mô tả**: Người dùng tạo tài khoản mới bằng cách cung cấp địa chỉ email, tên hiển thị và mật khẩu bảo mật để bắt đầu lưu trữ nhật ký cá nhân trực tuyến.
- **Tiền điều kiện**: Thiết bị có kết nối mạng ổn định và email đăng ký chưa tồn tại trên hệ thống.

| Luồng chính (Main Flow) | Luồng ngoại lệ (Exception Flow) |
| :--- | :--- |
| 1. Người dùng chọn "Đăng ký" tại màn hình chờ.<br>2. Nhập các thông tin: Email, Tên người dùng, Mật khẩu và Xác nhận mật khẩu.<br>3. Nhấn nút "Đăng ký".<br>4. Hệ thống gửi yêu cầu lên Server xác thực định dạng dữ liệu và kiểm tra trùng lặp email.<br>5. Hệ thống tạo tài khoản mới, cấp token đăng nhập và lưu thông tin người dùng lên Server.<br>6. Hệ thống điều hướng người dùng tới màn hình Hướng dẫn thiết lập ban đầu (Onboarding) và hiển thị thông báo đăng ký thành công. | - **Mật khẩu không khớp**: Hệ thống hiển thị lỗi "Mật khẩu không khớp" và yêu cầu nhập lại mật khẩu xác nhận.<br>- **Mật khẩu quá ngắn**: Nhập mật khẩu dưới 6 ký tự, hệ thống báo lỗi "Mật khẩu phải có ít nhất 6 ký tự".<br>- **Định dạng email sai**: Nhập email không hợp lệ, hệ thống báo "Định dạng email không hợp lệ".<br>- **Email đã tồn tại**: Server trả về mã lỗi xung đột, hệ thống thông báo "Email đã được sử dụng" và đề xuất đăng nhập hoặc dùng email khác. |

- **Hậu điều kiện**: Bản ghi người dùng được thêm vào cơ sở dữ liệu trên máy chủ, người dùng được đăng nhập tự động và token được lưu trữ cục bộ qua DataStore Preferences.

---

### UC-QL-02: Đăng nhập hệ thống (Google SSO)
- **Actor**: Người dùng phổ thông (AT1)
- **Mô tả**: Người dùng sử dụng tài khoản Google cá nhân để đăng nhập trực tiếp vào ứng dụng mà không cần điền email và mật khẩu thủ công.
- **Tiền điều kiện**: Thiết bị có cài đặt Google Play Services và kết nối Internet.

| Luồng chính (Main Flow) | Luồng ngoại lệ (Exception Flow) |
| :--- | :--- |
| 1. Người dùng chọn "Đăng nhập bằng Google" tại màn hình đăng nhập.<br>2. Hệ thống gọi SDK Credential Manager để hiển thị hộp thoại chọn tài khoản Google liên kết trên thiết bị.<br>3. Người dùng chọn tài khoản muốn đăng nhập.<br>4. Ứng dụng nhận ID Token từ Google SDK và gửi lên endpoint `/api/auth/google-login` trên máy chủ.<br>5. Máy chủ xác thực token Google, nếu là tài khoản mới sẽ tự động đăng ký với thông tin tên và avatar lấy từ Google, sau đó trả về JWT Token và hồ sơ người dùng.<br>6. Ứng dụng lưu JWT Token, tải cấu hình giao diện hiện tại của người dùng và chuyển hướng vào màn hình Lịch (Calendar) trang chủ. | - **Người dùng hủy chọn tài khoản**: Người dùng tắt popup chọn tài khoản Google, ứng dụng hủy luồng đăng nhập và giữ nguyên trạng thái màn hình.<br>- **Lỗi xác thực Google**: Lỗi mạng hoặc token không hợp lệ, hệ thống hiển thị thông báo "Xác nhận Google thất bại" và yêu cầu thử lại. |

- **Hậu điều kiện**: Người dùng đăng nhập thành công vào ứng dụng, phiên làm việc được lưu giữ và dữ liệu cá nhân được đồng bộ với Server.

---

### UC-QL-03: Quên mật khẩu & Đặt lại mật khẩu (Forgot Password via Email OTP)
- **Actor**: Người dùng phổ thông (AT1)
- **Mô tả**: Người dùng khôi phục lại mật khẩu đăng nhập của mình thông qua xác minh mã OTP gửi về Email đã đăng ký.
- **Tiền điều kiện**: Người dùng chưa đăng nhập, đang ở màn hình Đăng nhập và tài khoản ứng dụng đã tồn tại.

| Luồng chính (Main Flow) | Luồng ngoại lệ (Exception Flow) |
| :--- | :--- |
| 1. Người dùng nhấn nút "Quên mật khẩu?" tại màn hình đăng nhập.<br>2. Người dùng điền Email đăng ký và nhấn "Gửi OTP".<br>3. Hệ thống gửi yêu cầu qua API `/api/auth/forgot-password`. Máy chủ gửi 1 mã OTP 6 chữ số đến hòm thư email của người dùng.<br>4. Người dùng kiểm tra email, điền mã OTP vào app và nhấn "Xác nhận & Tiếp tục".<br>5. Ứng dụng gửi yêu cầu xác thực OTP qua API `/api/auth/verify-otp` và nhận về chuỗi mã `resetToken`.<br>6. Ứng dụng chuyển hướng người dùng đến màn hình "Đặt lại mật khẩu".<br>7. Người dùng điền mật khẩu mới và xác nhận mật khẩu mới, sau đó nhấn "Đặt lại mật khẩu".<br>8. Ứng dụng gửi mật khẩu mới kèm `resetToken` qua API `/api/auth/reset-password` lên máy chủ.<br>9. Máy chủ cập nhật mật khẩu mới thành công, hiển thị popup chúc mừng và đưa người dùng về lại màn hình Đăng nhập. | - **Email không tồn tại**: Nếu email nhập vào chưa đăng ký, Backend trả về lỗi 404, ứng dụng hiển thị thông báo lỗi và yêu cầu kiểm tra lại email.<br>- **Nhập sai mã OTP/Mã hết hạn**: Nếu mã OTP nhập không chính xác hoặc hết hiệu lực, ứng dụng hiển thị lỗi "Mã OTP không hợp lệ". Người dùng có thể nhấn "Gửi lại ngay" để nhận mã mới.<br>- **Mật khẩu mới không trùng khớp**: Nếu mật khẩu xác nhận không khớp mật khẩu mới hoặc ngắn hơn 6 ký tự, ứng dụng hiển thị cảnh báo đỏ ngay trên ô nhập liệu và khóa nút Đặt lại mật khẩu. |

- **Hậu điều kiện**: Mật khẩu của tài khoản người dùng được cập nhật mới trên Server API, sẵn sàng đăng nhập bằng mật khẩu mới.

---

### UC-QL-04: Cấu hình khóa bảo mật ứng dụng (PIN & Sinh trắc học)
- **Actor**: Người dùng phổ thông (AT1)
- **Mô tả**: Người dùng thiết lập mã PIN 4 số bảo mật và kích hoạt xác thực vân tay/khuôn mặt trên thiết bị di động để khóa riêng tư khi mở app.
- **Tiền điều kiện**: Người dùng đã đăng nhập thành công và đang ở màn hình Cài đặt Bảo mật của app.

| Luồng chính (Main Flow) | Luồng ngoại lệ (Exception Flow) |
| :--- | :--- |
| 1. Người dùng chọn mục "Khóa mã PIN".<br>2. Hệ thống chuyển đến màn hình "Tạo mã PIN", yêu cầu nhập mã PIN gồm 4 chữ số.<br>3. Người dùng nhập mã PIN lần 1, sau đó hệ thống hiển thị màn hình "Xác nhận mã PIN" yêu cầu nhập lại lần 2.<br>4. Nếu hai lần khớp nhau, ứng dụng mã hóa mã PIN và lưu cục bộ vào DataStore Preferences và kích hoạt khóa ứng dụng bằng PIN.<br>5. (Tùy chọn) Người dùng bật công tắc "Xác thực vân tay" (Biometrics).<br>6. Hệ thống gọi SDK hệ điều hành hiển thị popup xác thực vân tay hệ thống.<br>7. Người dùng chạm vân tay vào cảm biến thành công, ứng dụng lưu trạng thái vân tay cục bộ.<br>8. Ở những lần khởi chạy ứng dụng tiếp theo, ứng dụng tự động hiển thị màn hình khóa yêu cầu nhập PIN hoặc vân tay trước khi truy cập trang chính. | - **Mã PIN xác nhận không trùng khớp**: Lần nhập 2 khác lần 1, ứng dụng hiển thị thông báo "Mã PIN không khớp. Vui lòng thử lại" và xóa trường để nhập lại từ đầu.<br>- **Thiết bị chưa cấu hình vân tay hoặc không hỗ trợ**: Người dùng bật vân tay nhưng máy chưa thiết lập vân tay trong Android Settings hoặc không có phần cứng, ứng dụng báo "Thiết bị không hỗ trợ sinh trắc học hoặc bạn chưa thiết lập". |

- **Hậu điều kiện**: Trạng thái bảo mật được lưu trữ cục bộ trên thiết bị qua DataStore Preferences. Màn hình khóa PIN/Vân tay được kích hoạt hiệu lực.

---

### UC-QL-05: Cài đặt và Tùy chỉnh ứng dụng (Settings Preferences)
- **Actor**: Người dùng phổ thông (AT1), Hệ thống tự động (AT3)
- **Mô tả**: Người dùng thay đổi các cấu hình hiển thị của ứng dụng bao gồm đổi ngôn ngữ (Tiếng Anh/Việt), bật theo dõi chu kỳ kinh nguyệt, và cấu hình các hiển thị của Glance Widgets.
- **Tiền điều kiện**: Người dùng ở màn hình Cài đặt (Settings) của ứng dụng.

| Luồng chính (Main Flow) | Luồng ngoại lệ (Exception Flow) |
| :--- | :--- |
| 1. Người dùng chọn menu "Cài đặt" hoặc "Tùy chỉnh".<br>2. Người dùng thực hiện cấu hình các thông số:<br>&nbsp;&nbsp;&nbsp;&nbsp;- Chọn **Ngôn ngữ**: Đổi từ Tiếng Anh sang Tiếng Việt hoặc ngược lại. Ứng dụng tự động cập nhật Locale hệ thống, vẽ lại (recompose) toàn bộ giao diện theo gói ngôn ngữ mới.<br>&nbsp;&nbsp;&nbsp;&nbsp;- Bật/Tắt **Chu kỳ kinh nguyệt**: Tắt hiển thị theo dõi chu kỳ kinh nguyệt nếu không có nhu cầu sử dụng.<br>&nbsp;&nbsp;&nbsp;&nbsp;- Bật/Tắt **Huy hiệu Widget**: Chọn hiển thị hoặc ẩn số ngày chuỗi (Streak) trên Glance widget.<br>3. Ứng dụng lưu cấu hình mới ngay lập tức vào local preferences DataStore thông qua `SettingsPreferencesManager`. | - **Lỗi đồng bộ giao diện Widget**: Do hạn chế tần suất cập nhật widget của hệ điều hành Android Glance, widget có thể bị chậm trễ khi làm mới. Hệ thống ghi nhận yêu cầu làm mới và thực thi cập nhật ngay khi được hệ điều hành cho phép. |

- **Hậu điều kiện**: Cấu hình tùy chỉnh được cập nhật vĩnh viễn vào bộ nhớ cục bộ thiết bị di động, thay đổi trực tiếp giao diện ứng dụng và các Glance Widgets tương ứng.

---

### UC-NK-01: Tạo mới/Cập nhật nhật ký ngày (Upsert Daily Log)
- **Actor**: Người dùng phổ thông (AT1), Hệ thống tự động (AT3)
- **Mô tả**: Người dùng ghi chép nhật ký cho một ngày cụ thể (mặc định là hôm nay hoặc ngày trong quá khứ) bao gồm tâm trạng, thẻ hoạt động, ghi chú, ảnh chụp, thông tin giấc ngủ, chu kỳ kinh nguyệt, nhạc Spotify và sức khỏe.
- **Tiền điều kiện**: Người dùng đã đăng nhập và đang ở giao diện soạn thảo nhật ký.

| Luồng chính (Main Flow) | Luồng ngoại lệ (Exception Flow) |
| :--- | :--- |
| 1. Người dùng chọn một ngày trên Lịch biểu và nhấn "Viết nhật ký" (hoặc nhấn "Sửa" nếu ngày đó đã có nhật ký).<br>2. Chọn 1 trong 5 mức độ cảm xúc Cute Bean đại diện.<br>3. Chọn các thẻ hoạt động thực hiện trong ngày (quét chọn theo danh mục: sở thích, mối quan hệ, việc nhà, v.v.).<br>4. Viết nội dung văn bản vào phần Ghi chú (Note).<br>5. (Tùy chọn) Chọn ảnh đính kèm từ thư viện hoặc chụp qua CameraX.<br>6. (Tùy chọn) Điền thời gian đi ngủ, thức dậy để tính số giờ ngủ; Bật theo dõi chu kỳ kinh nguyệt.<br>7. (Tùy chọn) Nhấn "Nhập" để đồng bộ dữ liệu bước chân, calo, khoảng cách từ Health Connect; Nhấn "Thêm bài hát" để tìm bài hát qua Spotify.<br>8. Hệ thống tự động xác định thời tiết và nhiệt độ hiện tại qua GPS (nếu được cấp quyền).<br>9. Nhấn nút "Lưu".<br>10. Hệ thống lưu bản ghi vào bảng local `daily_logs` qua Room DB, cập nhật Glance Widgets màn hình chính, đồng thời gửi dữ liệu lên endpoint `/api/dailylogs` để đồng bộ hóa với máy chủ Backend. | - **Ghi nhật ký ngày tương lai**: Chọn ngày lớn hơn ngày hiện tại, hệ thống báo lỗi "Bạn không thể ghi chép cho ngày trong tương lai!" và ngăn chặn lưu.<br>- **Ghi đè bản ghi cũ**: Nếu ngày được chọn đã tồn tại nhật ký, hệ thống hiển thị cảnh báo "Đã có bản ghi cho ngày này. Bạn có muốn ghi đè không?". Nếu người dùng chọn "Ghi đè", hệ thống thực hiện cập nhật bản ghi cũ. Nếu chọn "Hủy", ứng dụng quay lại màn hình soạn thảo.<br>- **Mất kết nối mạng**: Khi lưu nhật ký mà không có mạng, hệ thống lưu thành công vào Room DB cục bộ, ghi nhận trạng thái chờ đồng bộ và hiển thị thông báo "Đã lưu ngoại tuyến. Nhật ký sẽ được đồng bộ khi có kết nối". |

- **Hậu điều kiện**: Nhật ký được ghi nhận thành công trong cơ sở dữ liệu. Chuỗi ngày viết nhật ký (Streak) của người dùng được cập nhật (+1 ngày) và tăng số xu thưởng (nếu có).

---

### UC-KK-01: Đăng tải khoảnh khắc (Post Moment)
- **Actor**: Người dùng phổ thông (AT1)
- **Mô tả**: Người dùng ghi lại một hình ảnh kỷ niệm đẹp kèm chú thích ngắn để lưu vào khoảnh khắc cá nhân hoặc chia sẻ công khai lên bảng tin cộng đồng.
- **Tiền điều kiện**: Thiết bị đã cấp quyền sử dụng camera và lưu trữ.

| Luồng chính (Main Flow) | Luồng ngoại lệ (Exception Flow) |
| :--- | :--- |
| 1. Người dùng vào tab Khoảnh khắc (Moment) và chọn biểu tượng Camera.<br>2. Trình CameraX hiển thị, người dùng chụp ảnh trực tiếp (hoặc bấm chọn tải lên ảnh từ bộ nhớ thiết bị).<br>3. Nhập văn bản chú thích (Caption) cho bức ảnh.<br>4. Bật hoặc tắt công tắc "Công khai" (IsPublic) để thiết lập quyền riêng tư.<br>5. Chọn nút "Đăng khoảnh khắc".<br>6. Hệ thống mã hóa ảnh dạng Multipart và gọi API POST `/api/moments` gửi dữ liệu lên máy chủ.<br>7. Máy chủ lưu trữ hình ảnh trên Cloud Storage, ghi nhận liên kết cơ sở dữ liệu và phản hồi trạng thái thành công.<br>8. Ứng dụng nhận phản hồi, hiển thị thông báo "Tải lên khoảnh khắc thành công!" và tải lại danh sách Moments. | - **Lỗi máy chủ/Không tải được ảnh**: Quá trình upload bị đứt quãng hoặc định dạng ảnh không được hỗ trợ, hệ thống hiển thị thông báo lỗi và cho phép người dùng nhấn "Thử lại".<br>- **Không cấp quyền Camera**: Người dùng từ chối quyền truy cập camera, hệ thống hiển thị màn hình hướng dẫn và nút "Cho phép truy cập" để mở cài đặt thiết bị. |

- **Hậu điều kiện**: Khoảnh khắc mới được đăng tải thành công, hiển thị trên bảng tin cộng đồng (nếu đặt công khai) và kho lưu trữ của cá nhân.

---

### UC-CH-04: Thiết kế giao diện tùy chỉnh (Design Custom Theme)
- **Actor**: Người dùng phổ thông (AT1)
- **Mô tả**: Người dùng tự thiết kế một bộ giao diện độc quyền cho ứng dụng bằng các công cụ thay đổi hình nền, vẽ đồ họa canvas và gán màu sắc tùy chọn cho các biểu tượng.
- **Tiền điều kiện**: Người dùng có tối thiểu 250 xu để mở khóa một slot thiết kế theme tùy chỉnh.

| Luồng chính (Main Flow) | Luồng ngoại lệ (Exception Flow) |
| :--- | :--- |
| 1. Người dùng vào Cửa hàng -> chọn tab "Của tôi" -> nhấn "Tạo theme mới".<br>2. Hệ thống kiểm tra số dư xu, trừ 250 xu và hiển thị giao diện Trình soạn thảo (Custom Theme Editor).<br>3. Người dùng chọn công cụ **Nền** (Background):<br>&nbsp;&nbsp;&nbsp;&nbsp;- Chọn "Màu đồng nhất" và chọn màu qua bảng màu hoặc mã HEX.<br>&nbsp;&nbsp;&nbsp;&nbsp;- Chọn "Màu gradient" và chọn Màu đầu, Màu cuối.<br>&nbsp;&nbsp;&nbsp;&nbsp;- Chọn "Tải ảnh lên" từ thư viện, điều chỉnh thu phóng (scale), xoay (rotation) hoặc lật ảnh.<br>4. Chọn công cụ **Vẽ** (Draw): Chọn cọ vẽ (4 kiểu: Mảnh, Đậm, Chì, Phun), chọn màu cọ, vẽ tự do trên Canvas đè lên nền. Có thể dùng Tẩy (Eraser), Hoàn tác (Undo) hoặc Xóa nét vẽ (Clear) để chỉnh sửa nét vẽ.<br>5. Chọn công cụ **Màu sắc** (Colors): Chọn Màu chủ đạo (Primary Color) hệ thống; Chọn từng icon hạt trăng trong bộ 5 cảm xúc và chọn màu sắc tương ứng.<br>6. Chọn chế độ sáng/tối để xem trước cấu hình giao diện.<br>7. Nhấn "Xem trước" (Preview) để xem hiển thị toàn diện.<br>8. Điền tên giao diện tùy chỉnh và chọn "Lưu".<br>9. Hệ thống sinh tệp ảnh bitmap hình nền và hình thu nhỏ (thumbnail), lưu chúng vào bộ nhớ trong của thiết bị, ghi bản ghi vào bảng local `custom_themes` và gửi yêu cầu tạo theme lên máy chủ qua API. | - **Không đủ xu**: Nếu xu của người dùng nhỏ hơn 250, hệ thống ngăn cản vào trình tạo giao diện và hiển thị thông báo "Bạn không đủ xu, hãy chăm chỉ ghi nhật ký thêm nhé!".<br>- **Thoát mà chưa lưu**: Người dùng nhấn nút quay lại khi đang thiết kế, hệ thống hiển thị hộp thoại cảnh báo "Bạn có thay đổi chưa lưu. Bạn có chắc muốn hủy bỏ chúng?". Nếu chọn "Hủy bỏ", hệ thống thoát ra màn hình trước và bỏ qua các thay đổi. Nếu chọn "Hủy", hộp thoại đóng và tiếp tục thiết kế. |

- **Hậu điều kiện**: Bộ giao diện tùy chỉnh mới được tạo và lưu trữ thành công trên cả thiết bị cục bộ (Room DB) và máy chủ, sẵn sàng được kích hoạt sử dụng làm giao diện chính.

---

### UC-CH-03: Khôi phục chuỗi ngày viết nhật ký (Recover Streak)
- **Actor**: Người dùng phổ thông (AT1), Hệ thống tự động (AT3)
- **Mô tả**: Người dùng thực hiện nối lại chuỗi ngày viết nhật ký liên tiếp (Streak) bị đứt đoạn do quên viết nhật ký vào ngày hôm trước bằng cách sử dụng bình Đóng băng chuỗi (Streak Freeze).
- **Tiền điều kiện**: Chuỗi ghi chép bị đứt đoạn vào ngày hôm trước và người dùng đang sở hữu ít nhất 1 bình Đóng băng chuỗi.

| Luồng chính (Main Flow) | Luồng ngoại lệ (Exception Flow) |
| :--- | :--- |
| 1. Người dùng truy cập Cửa hàng giao diện, phần "Bảo vệ chuỗi".<br>2. Hệ thống hiển thị trạng thái: "Bạn đã lỡ viết nhật ký hôm qua. Bạn có thể dùng 1 bình đóng băng để nối lại chuỗi".<br>3. Người dùng nhấn nút "Dùng 1 lượt để khôi phục".<br>4. Ứng dụng gửi yêu cầu POST lên endpoint `/api/users/me/streak/recover` của máy chủ.<br>5. Máy chủ xác minh thông tin, thực hiện trừ 1 bình đóng băng chuỗi trong tài khoản người dùng, tính toán phục hồi chuỗi ngày ghi chép cũ và cộng thêm ngày hôm qua vào tiến trình.<br>6. Máy chủ phản hồi thành công và trả về chỉ số chuỗi mới.<br>7. Ứng dụng nhận kết quả, hiển thị popup "Chuỗi của bạn đã được nối lại" và cập nhật chỉ số streak mới trên giao diện và Glance Widgets. | - **Không có chuỗi bị đứt**: Người dùng nhấn khôi phục khi chuỗi vẫn được duy trì bình thường, API trả về lỗi "NO_BROKEN_STREAK", hệ thống hiển thị thông báo "Chuỗi của bạn vẫn an toàn" và không trừ bình đóng băng.<br>- **Không còn bình đóng băng**: Người dùng không có bình đóng băng nào, nút khôi phục sẽ bị vô hiệu hóa (disabled) kèm dòng chữ mô tả trạng thái "Bạn chưa có bình đóng băng nào để nối lại ngay lúc này". |

- **Hậu điều kiện**: Số lượng bình đóng băng chuỗi của người dùng giảm đi 1, chỉ số chuỗi ghi chép hiện tại được phục hồi và lưu trữ đồng bộ.

---

### UC-AD-03: Tải lên & Quản lý giao diện trong Cửa hàng
*(Chức năng quản lý chỉ dành cho Admin thực hiện thông qua Web Portal / Backend API, không có trong ứng dụng di động)*
- **Actor**: Quản trị viên (AT2)
- **Mô tả**: Quản trị viên tải lên tài nguyên hình ảnh giao diện mới, thiết lập giá bán xu và phân loại bộ sưu tập để hiển thị trong Cửa hàng giao diện cho toàn bộ người dùng hệ thống.
- **Tiền điều kiện**: Quản trị viên đã đăng nhập thành công bằng tài khoản có quyền ADMIN trên trang quản lý Web.

| Luồng chính (Main Flow) | Luồng ngoại lệ (Exception Flow) |
| :--- | :--- |
| 1. Admin truy cập trang Quản lý Giao diện trên giao diện quản trị Web.<br>2. Nhấn nút "Tải lên giao diện mới".<br>3. Nhập mã định danh giao diện (Id), Tên giao diện, Mô tả và Giá bán (bằng xu).<br>4. Tải lên tệp ảnh Thu nhỏ (Thumbnail) và ảnh Nền (Background) của giao diện.<br>5. Thiết lập màu sắc hiển thị hệ thống (màu chủ đạo sáng/tối) và tải lên tệp JSON cấu hình màu sắc chi tiết cho bộ 5 biểu tượng hạt trăng cảm xúc.<br>6. Chọn bộ sưu tập (Collection) và đặt trạng thái "Kích hoạt" (IsActive = true).<br>7. Nhấn nút "Lưu cấu hình".<br>8. Hệ thống gửi yêu cầu multipart-form lên API `/api/themes/upload`. Server xử lý lưu trữ tệp, tạo bản ghi trong bảng cơ sở dữ liệu `themes` và `theme_moods`. | - **Trùng lặp mã Id**: Id giao diện đã tồn tại trên Server, hệ thống báo lỗi trùng lặp và yêu cầu đổi Id khác.<br>- **Thiếu tệp hình ảnh bắt buộc**: Admin quên tải ảnh nền hoặc ảnh thu nhỏ, hệ thống hiển thị cảnh báo đỏ tại trường tương ứng và yêu cầu bổ sung trước khi lưu. |

- **Hậu điều kiện**: Giao diện mới được tạo thành công trên hệ thống và hiển thị trực tuyến trong Cửa hàng cho tất cả người dùng chọn mua.

---

## 5. CẤU TRÚC CƠ SỞ DỮ LIỆU

### 5.1 Bảng: daily_logs (Room Local Database - SQLite cục bộ thiết bị)
Bảng lưu trữ thông tin ghi chép nhật ký hàng ngày của người dùng trên thiết bị di động.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | VARCHAR | PRIMARY KEY | Mã định danh duy nhất của bản ghi nhật ký. |
| **baseMoodId** | INT | NOT NULL | Chỉ số cảm xúc gốc (1: Rất buồn, 2: Buồn, 3: Bình thường, 4: Vui, 5: Rất vui). |
| **date** | VARCHAR | NOT NULL | Ngày ghi nhật ký (Định dạng: `yyyy-MM-dd`). |
| **note** | TEXT | NULL | Nội dung văn bản ghi chú chi tiết trong ngày. |
| **sleepHours** | DOUBLE | NULL | Tổng số giờ ngủ được ghi nhận. |
| **sleepStartTime** | VARCHAR | NULL | Thời gian bắt đầu đi ngủ (Định dạng: `HH:mm`). |
| **wakeupTime** | VARCHAR | NULL | Thời gian thức dậy (Định dạng: `HH:mm`). |
| **isMenstruation** | BOOLEAN | NOT NULL | Đang trong giai đoạn kinh nguyệt hay không (TRUE/FALSE). |
| **menstruationPhase**| VARCHAR | NULL | Giai đoạn cụ thể của chu kỳ kinh nguyệt. |
| **dailyPhotosJson** | TEXT | NULL | Chuỗi JSON danh sách đường dẫn/URL ảnh đính kèm. |
| **activityIdsJson** | TEXT | NULL | Chuỗi JSON danh sách mã các thẻ hoạt động đã chọn. |
| **steps** | INT | NULL | Số bước chân đồng bộ từ Health Connect. |
| **calories** | INT | NULL | Lượng calo tiêu hao đồng bộ từ Health Connect. |
| **distance** | DOUBLE | NULL | Quãng đường di chuyển (km) từ Health Connect. |
| **musicRecord** | VARCHAR | NULL | Dữ liệu văn bản tên bài hát kèm nghệ sĩ. |
| **musicTitle** | VARCHAR | NULL | Tên bài hát liên kết từ Spotify. |
| **artistName** | VARCHAR | NULL | Tên nghệ sĩ hát liên kết từ Spotify. |
| **albumArtUrl** | VARCHAR | NULL | Đường dẫn URL hình ảnh bìa album nhạc Spotify. |
| **weather** | VARCHAR | NULL | Trạng thái thời tiết ghi nhận (nắng, mưa, nhiều mây,...). |
| **temperature** | DOUBLE | NULL | Chỉ số nhiệt độ ghi nhận (°C). |
| **createdAt** | VARCHAR | NULL | Thời điểm tạo bản ghi. |

---

### 5.2 Bảng: themes (Room Local Database - SQLite cục bộ thiết bị)
Bảng lưu trữ thông tin các bộ giao diện hệ thống được tải xuống từ máy chủ.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | VARCHAR | PRIMARY KEY | Mã định danh duy nhất của giao diện. |
| **name** | VARCHAR | NOT NULL | Tên hiển thị của giao diện. |
| **collection** | VARCHAR | NOT NULL | Bộ sưu tập thuộc về (ví dụ: Classic, Summer, Custom). |
| **price** | INT | NOT NULL | Giá bán bằng xu (0 nếu là giao diện miễn phí). |
| **isFree** | BOOLEAN | NOT NULL | Trạng thái miễn phí hay trả phí. |
| **thumbnailUrl** | VARCHAR | NULL | Đường dẫn ảnh thu nhỏ hiển thị trong danh sách Store. |
| **backgroundUrl** | VARCHAR | NULL | Đường dẫn ảnh nền chính của giao diện. |
| **isOwned** | BOOLEAN | NOT NULL | Người dùng đã mua/sở hữu giao diện này hay chưa. |
| **isActive** | BOOLEAN | NOT NULL | Có đang là giao diện hoạt động hiện tại của app hay không. |
| **description** | TEXT | NULL | Mô tả chi tiết giao diện hoặc chuỗi cấu hình JSON. |
| **type** | VARCHAR | NOT NULL | Phân loại kiểu giao diện (THEME hoặc ICON_PACK). |
| **icons** | VARCHAR | NOT NULL | Chuỗi danh sách tên các biểu tượng cảm xúc đi kèm. |
| **primaryColor** | VARCHAR | NULL | Mã màu Hex chủ đạo của giao diện (ví dụ: `#FF8D6E63`). |
| **decoration** | VARCHAR | NOT NULL | Kiểu trang trí bổ sung của giao diện. |
| **activatedAt** | BIGINT | NULL | Thời điểm giao diện này được người dùng kích hoạt (ms). |

---

### 5.3 Bảng: theme_moods (Room Local Database - SQLite cục bộ thiết bị)
Bảng lưu trữ cấu hình màu sắc hoặc URL hình ảnh biểu tượng cảm xúc tùy chỉnh theo từng bộ giao diện.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **themeId** | VARCHAR | PRIMARY KEY, FK | Khóa ngoại liên kết tới bảng `themes.id` (ON DELETE CASCADE). |
| **baseMoodId** | VARCHAR | PRIMARY KEY | Mã mức độ cảm xúc (1 đến 5). |
| **iconUrl** | VARCHAR | NOT NULL | Đường dẫn ảnh biểu tượng cảm xúc tùy chỉnh. |
| **customName** | VARCHAR | NOT NULL | Tên tùy chỉnh của mức độ cảm xúc tương ứng. |

---

### 5.4 Bảng: custom_themes (Room Local Database - SQLite cục bộ thiết bị)
Bảng lưu trữ dữ liệu thiết kế các giao diện tự vẽ/tự phối màu do chính người dùng sáng tạo cục bộ.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | VARCHAR | PRIMARY KEY | Mã định danh duy nhất của giao diện tự thiết kế. |
| **name** | VARCHAR | NOT NULL | Tên do người dùng đặt cho giao diện. |
| **bgFilePath** | VARCHAR | NOT NULL | Đường dẫn tệp ảnh nền được kết xuất sau khi vẽ/tải ảnh. |
| **primaryColor** | VARCHAR | NOT NULL | Mã màu Hex chủ đạo được phối. |
| **iconColor** | VARCHAR | NOT NULL | Màu sắc gán chung cho các biểu tượng. |
| **iconColors** | VARCHAR | NOT NULL | Chuỗi mã màu Hex của 5 biểu tượng (phân cách bằng dấu phẩy). |
| **lightConfigJson** | TEXT | NOT NULL | Chuỗi JSON cấu hình chi tiết cho chế độ hiển thị Sáng. |
| **darkConfigJson** | TEXT | NOT NULL | Chuỗi JSON cấu hình chi tiết cho chế độ hiển thị Tối. |
| **createdAt** | BIGINT | NOT NULL | Thời điểm khởi tạo giao diện tùy chỉnh (ms). |

---

### 5.5 Bảng: statistics (Room Local Database - SQLite cục bộ thiết bị)
Bảng lưu trữ dữ liệu phân tích thống kê được lưu tạm (cache) để tăng tốc độ tải màn hình báo cáo.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **userId** | VARCHAR | PRIMARY KEY | Mã người dùng sở hữu dữ liệu thống kê. |
| **year** | INT | PRIMARY KEY | Năm thống kê dữ liệu. |
| **month** | INT | PRIMARY KEY | Tháng thống kê dữ liệu. |
| **isMonthly** | BOOLEAN | PRIMARY KEY | Trạng thái là thống kê tháng hay thống kê năm (TRUE/FALSE). |
| **response** | TEXT | NOT NULL | Chuỗi JSON phản hồi chứa toàn bộ chỉ số thống kê từ Server. |
| **timestamp** | BIGINT | NOT NULL | Thời điểm lưu bản ghi bộ nhớ tạm này (ms). |

---

### 5.6 Bảng: users (Remote Server API Database - Máy chủ Backend)
Bảng lưu trữ hồ sơ tài khoản người dùng và trạng thái chuỗi ngày viết nhật ký trên máy chủ chính.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | UUID | PRIMARY KEY | Khóa chính, mã định danh duy nhất của người dùng. |
| **email** | VARCHAR | UNIQUE, NOT NULL | Địa chỉ email đăng nhập. |
| **password_hash** | VARCHAR | NULL | Chuỗi mật khẩu đã được mã hóa (chỉ có với tài khoản LOCAL). |
| **name** | VARCHAR | NOT NULL | Tên hiển thị người dùng. |
| **role** | VARCHAR | DEF 'User' | Vai trò tài khoản trên hệ thống (User hoặc Admin). |
| **avatarUrl** | VARCHAR | NULL | Đường dẫn URL hình ảnh đại diện người dùng trên máy chủ. |
| **gender** | VARCHAR | NULL | Giới tính (Male, Female, Other, Not Specified). |
| **birthday** | VARCHAR | NULL | Ngày sinh nhật (Định dạng: `yyyy-MM-dd`). |
| **coinBalance** | INT | DEF 0 | Số dư xu tích lũy (Moon Coins). |
| **currentStreak** | INT | DEF 0 | Số ngày liên tục hiện tại đang duy trì ghi nhật ký. |
| **longestStreak** | INT | DEF 0 | Kỷ lục chuỗi ngày liên tục viết nhật ký dài nhất. |
| **streakFreezes** | INT | DEF 0 | Số lượng bình Đóng băng chuỗi hiện có. |
| **recoverableStreak**| INT | DEF 0 | Chỉ số chuỗi có thể khôi phục lại (nếu có). |
| **activeThemeId** | VARCHAR | NULL | Mã giao diện người dùng đang áp dụng hoạt động. |
| **authProvider** | VARCHAR | DEF 'Password' | Nhà cung cấp xác thực (Password hoặc Google). |
| **createdAt** | TIMESTAMP | DEF NOW() | Thời điểm tạo lập tài khoản. |

---

### 5.7 Bảng: moments (Remote Server API Database - Máy chủ Backend)
Bảng lưu trữ thông tin các Khoảnh khắc ảnh được đăng tải lên bảng tin cộng đồng.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | UUID | PRIMARY KEY | Khóa chính, mã định danh duy nhất của Moment. |
| **userId** | UUID | FK | Mã người dùng đăng tải khoảnh khắc (liên kết `users.id`). |
| **userName** | VARCHAR | NOT NULL | Tên hiển thị người đăng khoảnh khắc tại thời điểm đăng. |
| **userAvatarUrl** | VARCHAR | NULL | URL ảnh đại diện người đăng khoảnh khắc. |
| **imageUrl** | VARCHAR | NOT NULL | URL hình ảnh khoảnh khắc lưu trữ trên Cloud Storage. |
| **caption** | VARCHAR | NULL | Chú thích ngắn đi kèm hình ảnh. |
| **isPublic** | BOOLEAN | DEF TRUE | Thiết lập quyền xem (TRUE: Công khai, FALSE: Riêng tư). |
| **capturedAt** | TIMESTAMP | DEF NOW() | Thời điểm ghi lại/chụp bức ảnh này. |
| **dailyLogId** | VARCHAR | NULL | Mã nhật ký ngày liên kết (nếu có). |
| **location** | VARCHAR | NULL | Tên địa điểm đăng khoảnh khắc. |
| **weather** | VARCHAR | NULL | Thông tin thời tiết tại địa điểm chụp. |
| **rating** | FLOAT | NULL | Điểm xếp hạng/đánh giá khoảnh khắc. |

---

### 5.8 Bảng: notifications (Remote Server API Database - Máy chủ Backend)
Bảng lưu trữ lịch sử các thông báo trong ứng dụng gửi cho người dùng.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | UUID | PRIMARY KEY | Khóa chính, mã định danh duy nhất của thông báo. |
| **userId** | UUID | FK | Mã người dùng nhận thông báo (liên kết `users.id`). |
| **title** | VARCHAR | NOT NULL | Tiêu đề của thông báo. |
| **message** | TEXT | NOT NULL | Nội dung chi tiết của thông báo. |
| **type** | VARCHAR | DEF 'System' | Loại thông báo (System, Streak, Reminder, Moment). |
| **isRead** | BOOLEAN | DEF FALSE | Trạng thái đã đọc hay chưa (TRUE/FALSE). |
| **createdAt** | TIMESTAMP | DEF NOW() | Thời điểm gửi thông báo. |

---

### 5.9 Bảng: activities (Remote Server API Database - Máy chủ Backend)
Bảng lưu trữ danh mục thẻ hoạt động dùng chung trên toàn hệ thống để người dùng chọn khi viết nhật ký.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | VARCHAR | PRIMARY KEY | Mã định danh duy nhất của thẻ hoạt động (ví dụ: `act_sport`). |
| **name** | VARCHAR | NOT NULL | Tên hoạt động hiển thị (ví dụ: Đọc sách, Đi bộ, Ăn tối). |
| **iconUrl** | VARCHAR | NOT NULL | Đường dẫn URL hình ảnh biểu tượng hoạt động. |
| **category** | VARCHAR | NOT NULL | Danh mục hoạt động (hobbies, emotions, meals, chores,...). |
