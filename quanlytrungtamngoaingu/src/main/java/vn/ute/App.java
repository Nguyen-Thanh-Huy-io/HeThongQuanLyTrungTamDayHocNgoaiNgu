package vn.ute;

import vn.ute.service.ServiceManager;
import vn.ute.ui.LoginForm;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // 1. Khởi tạo Giao diện (theo cách của thầy)
        // Nếu bạn chưa có lớp UI, hãy dùng: UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        

        // 2. Khởi tạo ServiceManager (Container chứa 13 services & TransactionManager)
        // ServiceManager bên trong sẽ khởi tạo các JpaRepository tương ứng 
        // giống như cách thầy làm: new JpaProductRepository(), v.v.
        ServiceManager serviceManager = ServiceManager.getInstance();

        // 3. Chạy ứng dụng
        SwingUtilities.invokeLater(() -> {
            // Theo luồng chuẩn, ta mở LoginForm trước
            // Truyền serviceManager vào để LoginForm có thể dùng UserService kiểm tra đăng nhập
            LoginForm loginForm = new LoginForm(serviceManager);
            loginForm.setVisible(true);
        });
    }
}