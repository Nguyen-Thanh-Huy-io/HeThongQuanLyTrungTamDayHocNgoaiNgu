package vn.ute;

import vn.ute.service.ServiceManager;
import vn.ute.ui.LoginForm;
import vn.ute.ui.UIUtils;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
        // 1. Khởi tạo giao diện và theme chung
        // chúng ta dùng FlatLaf để có phong cách hiện đại, có thể đổi sang FlatDarkLaf nếu muốn
        UIUtils.initLookAndFeel();

        // nếu vì lý do nào đó FlatLaf không load được thì fallback về native L&amp;F
        try {
            if (UIManager.getLookAndFeel() == null) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
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