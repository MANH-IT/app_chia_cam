# Chia Cam 🍊 - Ứng dụng Chia Hóa Đơn Thông Minh

**Chia Cam** là một ứng dụng di động mạnh mẽ và tiện lợi được thiết kế để giúp bạn và bạn bè dễ dàng quản lý, chia sẻ chi phí và hóa đơn trong các cuộc vui, bữa ăn hay chuyến du lịch. Tên gọi "Chia Cam" mang ý nghĩa vừa là "chia cam" (chia sẻ ngọt bùi), vừa là "chia bill" một cách công bằng và minh bạch.

---

## 🚀 Tính Năng Chính

*   **📸 Quét Hóa Đơn Bằng OCR:** Sử dụng Google ML Kit để nhận diện văn bản và tự động trích xuất số tiền từ ảnh chụp hóa đơn, tiết kiệm thời gian nhập liệu thủ công.
*   **⚖️ Kiểu Chia Linh Hoạt:** hỗ trợ nhiều phương thức chia tiền khác nhau:
    *   **Chia đều:** Chia đều số tiền cho tất cả thành viên được chọn.
    *   **Chia theo phần trăm:** Tùy chỉnh tỷ lệ đóng góp của mỗi người.
    *   **Chia theo số tiền tùy chỉnh:** Nhập chính xác số tiền mỗi người cần trả.
*   **👥 Quản Lý Nhóm:** Tạo và quản lý các nhóm bạn bè, đồng nghiệp hoặc gia đình để dễ dàng theo dõi chi tiêu chung.
*   **📊 Tối Ưu Hóa Công Nợ:** Thuật toán thông minh giúp đơn giản hóa các giao dịch giữa các thành viên (ví dụ: A nợ B, B nợ C -> A trả trực tiếp cho C).
*   **📱 Hoạt Động Ngoại Tuyến:** Sử dụng Room Database để lưu trữ dữ liệu cục bộ, giúp bạn tạo hóa đơn ngay cả khi không có kết nối internet.
*   **🔔 Thông Báo & Tương Tác:** Cập nhật tức thời các hóa đơn mới và phản hồi (reaction) từ bạn bè qua hệ thống thông báo sinh động.

---

## 🛠️ Công Nghệ Sử Dụng

*   **Ngôn ngữ:** Java
*   **Cơ sở dữ liệu:** Room Database (SQLite)
*   **Xử lý ảnh & OCR:** Google ML Kit Text Recognition
*   **Tải ảnh:** Glide
*   **Giao diện:** Material Components, CoordinatorLayout, ViewPager2
*   **Quản lý luồng:** AsyncTask (legacy) & ThreadPool

---

## 📂 Cấu Trúc Dự Án

*   `activities/`: Các màn hình chính của ứng dụng (Camera, Add Bill, Debt calculation, v.v.).
*   `adapters/`: Quản lý hiển thị dữ liệu cho các danh sách (RecyclerView).
*   `database/`: Cấu hình Room DB, Entities và DAOs.
*   `models/`: Định nghĩa các đối tượng dữ liệu (Bill, Member, Transaction, v.v.).
*   `utils/`: Các tiện ích hỗ trợ (Xử lý ảnh, OCR, Định dạng tiền tệ, Tính toán công nợ).
*   `fragments/`: Các thành phần giao diện nhỏ hơn (Feed, Profile, Notifications).

---

## ⚙️ Cài Đặt

1.  Clone repository này về máy:
    ```bash
    git clone https://github.com/MANH-IT/app_chia_cam.git
    ```
2.  Mở dự án bằng **Android Studio** (phiên bản Ladybug hoặc mới hơn).
3.  Đồng bộ Gradle và Build dự án.
4.  Chạy trên máy ảo hoặc thiết bị Android thật (đề nghị API 24 trở lên).

---

## 🤝 Đóng Góp

Mọi đóng góp nhằm cải thiện ứng dụng đều được hoan nghênh. Bạn có thể mở một Issue hoặc gửi Pull Request nếu có ý tưởng mới hoặc phát hiện lỗi.

---

## 📄 Giấy Phép

Dự án này được phát triển cho mục đích học tập và quản lý chi tiêu cá nhân.

---
*Phát triển bởi đội ngũ Chia Cam Team.*
