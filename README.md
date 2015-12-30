# Giải pháp tối ưu lưu lượng dữ liệu của người dùng
-Lưu lại dữ liệu địa điểm của người dùng (với sự đồng ý của người dùng), sau đó lưu các địa điểm mà người dùng đã tìm kiếm, nếu lần tìm kiếm sau, người dùng không di chuyển ra khỏi bán kính 3km (có thể hơn tùy thuộc) của địa điểm đã lưu thì chỉ cần lấy dữ liệu đã lưu cho người dùng. Nếu dữ liệu đã lưu không tồn tại thì mới sử dụng dữ liệu lấy mới từ Google.
-Những trường hợp đã test: di chuyển trong và ra khỏi bán kính 3Km, tắt rồi mở lại ứng dụng thì các dữ liệu vẫn tải được với điều kiện không có mạng internet,
