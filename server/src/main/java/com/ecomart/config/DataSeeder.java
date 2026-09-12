package com.ecomart.config;

import com.ecomart.domain.entity.*;
import com.ecomart.domain.enums.*;
import com.ecomart.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final CategoryRepository categoryRepository;
    private final MaterialRepository materialRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShopProperties shopProperties;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.admin-password:}")
    private String seedAdminPassword;

    @Value("${app.seed.customer-password:}")
    private String seedCustomerPassword;

    public DataSeeder(UserRepository userRepository,
                      CustomerRepository customerRepository,
                      CartRepository cartRepository,
                      CartItemRepository cartItemRepository,
                      AddressRepository addressRepository,
                      OrderRepository orderRepository,
                      OrderItemRepository orderItemRepository,
                      PaymentRepository paymentRepository,
                      ReviewRepository reviewRepository,
                      NotificationRepository notificationRepository,
                      CategoryRepository categoryRepository,
                      MaterialRepository materialRepository,
                      ProductRepository productRepository,
                      PasswordEncoder passwordEncoder,
                      ShopProperties shopProperties) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
        this.notificationRepository = notificationRepository;
        this.categoryRepository = categoryRepository;
        this.materialRepository = materialRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
        this.shopProperties = shopProperties;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedDemoUsers();
        seedCategories();
        seedMaterials();
        seedProducts();
        seedAddresses();
        seedCartItems();
        seedOrders();
        seedReviews();
    }

    // ---------------------------------------------------------------- users

    private void seedDemoUsers() {
        seedAdmin();
        seedStaff();
        seedCustomers();
    }

    private void seedStaff() {
        staff("staff", "staff@ecomart.vn", "0902000000");
        staff("staff2", "staff2@ecomart.vn", "0902000001");
    }

    private void staff(String username, String email, String phone) {
        if (!seedEnabled || userRepository.existsByUsername(username)) {
            return;
        }
        String password = seedAdminPassword != null && !seedAdminPassword.isBlank()
                ? seedAdminPassword : "Staff@123";
        Staff s = new Staff();
        s.setUsername(username);
        s.setEmail(email);
        s.setNumberPhone(phone);
        s.setPasswordHash(passwordEncoder.encode(password));
        s.setRole(UserRole.STAFF);
        s.setActive(true);
        s.setHireDate(LocalDate.now().minusDays(60L));
        userRepository.save(s);
    }

    private void seedAdmin() {
        admin("admin", "admin@ecomart.vn", "0900000000");
        admin("admin2", "admin2@ecomart.vn", "0901000001");
        admin("admin3", "admin3@ecomart.vn", "0901000002");
        admin("admin4", "admin4@ecomart.vn", "0901000003");
        admin("admin5", "admin5@ecomart.vn", "0901000004");
    }

    private void admin(String username, String email, String phone) {
        if (!seedEnabled || userRepository.existsByUsername(username)) {
            return;
        }
        String password = seedAdminPassword != null && !seedAdminPassword.isBlank()
                ? seedAdminPassword : "Admin@123";
        Admin a = new Admin();
        a.setUsername(username);
        a.setEmail(email);
        a.setNumberPhone(phone);
        a.setPasswordHash(passwordEncoder.encode(password));
        a.setRole(UserRole.ADMIN);
        a.setActive(true);
        a.setHireDate(LocalDate.now().minusDays(30L * (username.equals("admin") ? 400 : 100)));
        userRepository.save(a);
    }

    private void seedCustomers() {
        for (int i = 1; i <= 8; i++) {
            customer("customer" + (i == 1 ? "" : i),
                    "customer" + (i == 1 ? "" : i) + "@ecomart.vn",
                    "090" + (1000000 + i));
        }
    }

    private void customer(String username, String email, String phone) {
        if (!seedEnabled || userRepository.existsByUsername(username)) {
            return;
        }
        String password = seedCustomerPassword != null && !seedCustomerPassword.isBlank()
                ? seedCustomerPassword : "Customer@123";
        Customer c = new Customer();
        c.setUsername(username);
        c.setEmail(email);
        c.setNumberPhone(phone);
        c.setPasswordHash(passwordEncoder.encode(password));
        c.setRole(UserRole.CUSTOMER);
        c.setActive(true);
        c = customerRepository.save(c);

        Cart cart = new Cart();
        cart.setCustomer(c);
        cart = cartRepository.save(cart);
        c.setCart(cart);
        customerRepository.save(c);
    }

    // ----------------------------------------------------------- categories

    private void seedCategories() {
        Category rau = category("Rau củ sạch", "rau-cu-sach", "leaf", 1);
        Category trai = category("Trái cây tươi", "trai-cay-tuoi", "apple-logo", 2);
        Category hangKho = category("Thực phẩm khô", "thuc-pham-kho", "package", 3);

        Category rauXanh = category("Rau xanh", "rau-xanh", null, 1);
        child(rau, rauXanh);

        Category cuQua = category("Củ quả", "cu-qua", null, 2);
        child(rau, cuQua);

        Category traiNhietDoi = category("Trái cây nhiệt đới", "trai-cay-nhiet-doi", null, 1);
        child(trai, traiNhietDoi);

        Category traiNhapKhau = category("Trái cây nhập khẩu", "trai-cay-nhap-khau", null, 2);
        child(trai, traiNhapKhau);

        Category traiCaySay = category("Trái cây sấy", "trai-cay-say", null, 3);
        child(trai, traiCaySay);

        Category nguCoc = category("Ngũ cốc", "ngu-coc", null, 1);
        child(hangKho, nguCoc);

        Category dauHat = category("Đậu & hạt", "dau-va-hat", null, 2);
        child(hangKho, dauHat);
    }

    private void child(Category parent, Category child) {
        child.setParent(parent);
        parent.getChildren().add(child);
        categoryRepository.save(parent);
        categoryRepository.save(child);
    }

    private Category category(String name, String slug, String icon, int order) {
        return categoryRepository.findBySlug(slug).orElseGet(() -> {
            Category c = new Category();
            c.setName(name);
            c.setSlug(slug);
            c.setIcon(icon);
            c.setDisplayOrder(order);
            c.setActive(true);
            return categoryRepository.save(c);
        });
    }

    // ------------------------------------------------------------ materials

    private void seedMaterials() {
        material("Giấy", MaterialType.RECYCLED);
        material("Nhựa", MaterialType.SYNTHETIC);
        material("Túi vải", MaterialType.NATURAL);
        material("Thủy tinh", MaterialType.RECYCLED);
        material("Hộp nhựa", MaterialType.SYNTHETIC);
        material("Mía / bã mía", MaterialType.ORGANIC);
        material("Lá chuối", MaterialType.ORGANIC);
    }

    private void material(String name, MaterialType type) {
        materialRepository.findByName(name).orElseGet(() -> {
            Material m = new Material();
            m.setName(name);
            m.setType(type);
            return materialRepository.save(m);
        });
    }

    // ------------------------------------------------------------- products

    private record SeedProduct(String name, String slug, double price, int stock, double weight, String origin,
                               String categorySlug, String... images) {}

    private static List<SeedProduct> seedProductList() {
        return List.of(
                // Rau xanh
                new SeedProduct("Rau muống sạch", "rau-muong-sach", 12000, 100, 200, "Đà Lạt", "rau-xanh",
                        "rau-muong-sach-1.webp",
                        "rau-muong-sach-2.webp"),
                new SeedProduct("Cải bó xôi", "cai-bo-xoi", 18000, 80, 150, "Đà Lạt", "rau-xanh",
                        "cai-bo-xoi-1.webp",
                        "cai-bo-xoi-2.webp"),
                new SeedProduct("Xà lách xoăn", "xa-lach-xoan", 22000, 60, 150, "Đà Lạt", "rau-xanh",
                        "xa-lach-xoan-1.webp"),
                new SeedProduct("Cải thảo", "cai-thao", 16000, 90, 300, "Mộc Châu", "rau-xanh",
                        "cai-thao-1.webp"),
                new SeedProduct("Rau ngót", "rau-ngot", 15000, 70, 200, "Lâm Đồng", "rau-xanh",
                        "rau-ngot-1.webp"),
                new SeedProduct("Cải ngọt", "cai-ngot", 14000, 85, 250, "Đà Lạt", "rau-xanh",
                        "cai-ngot-1.webp",
                        "cai-ngot-2.webp"),
                new SeedProduct("Rau mồng tơi", "rau-mong-toi", 13000, 75, 200, "Lâm Đồng", "rau-xanh",
                        "rau-mong-toi-1.webp",
                        "rau-mong-toi-2.webp"),
                // Củ quả
                new SeedProduct("Cà rốt Đà Lạt", "ca-rot-da-lat", 15000, 120, 250, "Đà Lạt", "cu-qua",
                        "ca-rot-da-lat-1.webp",
                        "ca-rot-da-lat-2.webp"),
                new SeedProduct("Khoai tây", "khoai-tay", 16000, 90, 300, "Lâm Đồng", "cu-qua",
                        "khoai-tay-1.webp"),
                new SeedProduct("Bí đỏ", "bi-do", 25000, 50, 1200, "Đà Lạt", "cu-qua",
                        "bi-do-1.webp"),
                new SeedProduct("Cà chua sạch", "ca-chua-sach", 28000, 80, 500, "Lâm Đồng", "cu-qua",
                        "ca-chua-sach-1.webp",
                        "ca-chua-sach-2.webp"),
                new SeedProduct("Dưa chuột", "dua-chuot", 12000, 100, 300, "Lâm Đồng", "cu-qua",
                        "dua-chuot-1.webp"),
                new SeedProduct("Hành tây", "hanh-tay", 18000, 90, 250, "Ninh Thuận", "cu-qua",
                        "hanh-tay-1.webp"),
                new SeedProduct("Ớt chuông", "ot-chuong", 32000, 60, 200, "Lâm Đồng", "cu-qua",
                        "ot-chuong-1.webp"),
                new SeedProduct("Khoai lang mật", "khoai-lang-mat", 20000, 110, 500, "Đà Lạt", "cu-qua",
                        "khoai-lang-mat-1.webp",
                        "khoai-lang-mat-2.webp"),
                // Trái cây nhiệt đới
                new SeedProduct("Cam sành Việt", "cam-sanh-viet", 25000, 60, 300, "Tây Ninh", "trai-cay-nhiet-doi",
                        "cam-sanh-viet-1.webp",
                        "cam-sanh-viet-2.webp"),
                new SeedProduct("Xoài cát Hòa Lộc", "xoai-cat-hoa-loc", 45000, 50, 500, "Tiền Giang", "trai-cay-nhiet-doi",
                        "xoai-cat-hoa-loc-1.webp",
                        "xoai-cat-hoa-loc-2.webp"),
                new SeedProduct("Chuối sứ", "chuoi-su", 18000, 150, 400, "Tiền Giang", "trai-cay-nhiet-doi",
                        "chuoi-su-1.webp"),
                new SeedProduct("Bưởi da xanh", "buoi-da-xanh", 35000, 40, 1300, "Bến Tre", "trai-cay-nhiet-doi",
                        "buoi-da-xanh-1.webp"),
                new SeedProduct("Dưa hấu ruột đỏ", "dua-hau-ruot-do", 30000, 30, 2500, "Long An", "trai-cay-nhiet-doi",
                        "dua-hau-ruot-do-1.webp"),
                new SeedProduct("Bơ sáp Đắk Lắk", "bo-sap-dak-lak", 38000, 0, 400, "Đắk Lắk", "trai-cay-nhiet-doi",
                        "bo-sap-dak-lak-1.webp",
                        "bo-sap-dak-lak-2.webp"),
                new SeedProduct("Sầu riêng Ri6", "sau-rieng-ri6", 120000, 0, 2000, "Tiền Giang", "trai-cay-nhiet-doi",
                        "sau-rieng-ri6-1.webp",
                        "sau-rieng-ri6-2.webp"),
                new SeedProduct("Thanh long ruột đỏ", "thanh-long-ruot-do", 28000, 55, 600, "Bình Thuận", "trai-cay-nhiet-doi",
                        "thanh-long-ruot-do-1.webp",
                        "thanh-long-ruot-do-2.webp"),
                // Trái cây nhập khẩu
                new SeedProduct("Táo Mỹ", "tao-my", 55000, 70, 200, "Mỹ", "trai-cay-nhap-khau",
                        "tao-my-1.webp"),
                new SeedProduct("Nho xanh không hạt", "nho-xanh-khong-hat", 60000, 60, 500, "Úc", "trai-cay-nhap-khau",
                        "nho-xanh-khong-hat-1.webp"),
                new SeedProduct("Lê Hàn Quốc", "le-han-quoc", 65000, 45, 400, "Hàn Quốc", "trai-cay-nhap-khau",
                        "le-han-quoc-1.webp"),
                new SeedProduct("Cherry Chile", "cherry-chile", 120000, 35, 500, "Chile", "trai-cay-nhap-khau",
                        "cherry-chile-1.webp",
                        "cherry-chile-2.webp"),
                new SeedProduct("Kiwi New Zealand", "kiwi-new-zealand", 75000, 40, 300, "New Zealand", "trai-cay-nhap-khau",
                        "kiwi-new-zealand-1.webp",
                        "kiwi-new-zealand-2.webp"),
                new SeedProduct("Cam vàng Úc", "cam-vang-uc", 58000, 50, 350, "Úc", "trai-cay-nhap-khau",
                        "cam-vang-uc-1.webp",
                        "cam-vang-uc-2.webp"),
                // Ngũ cốc
                new SeedProduct("Gạo lứt hữu cơ", "gao-lut-huu-co", 68000, 70, 1000, "An Giang", "ngu-coc",
                        "gao-lut-huu-co-1.webp"),
                new SeedProduct("Gạo ST25", "gao-st25", 89000, 60, 1000, "Sóc Trăng", "ngu-coc",
                        "gao-st25-1.webp",
                        "gao-st25-2.webp"),
                new SeedProduct("Yến mạch nguyên chất", "yen-mach-nguyen-chat", 45000, 80, 500, "Bắc Giang", "ngu-coc",
                        "yen-mach-nguyen-chat-1.webp"),
                new SeedProduct("Hạt quinoa hữu cơ", "hat-quinoa-huu-co", 98000, 40, 500, "Lâm Đồng", "ngu-coc",
                        "hat-quinoa-huu-co-1.webp",
                        "hat-quinoa-huu-co-2.webp"),
                new SeedProduct("Bột mì nguyên cám", "bot-mi-nguyen-cam", 35000, 65, 1000, "Hà Nội", "ngu-coc",
                        "bot-mi-nguyen-cam-1.webp",
                        "bot-mi-nguyen-cam-2.webp"),
                new SeedProduct("Gạo nếp than", "gao-nep-than", 55000, 55, 1000, "Tây Ninh", "ngu-coc",
                        "gao-nep-than-1.webp",
                        "gao-nep-than-2.webp"),
                // Đậu & hạt
                new SeedProduct("Đậu xanh", "dau-xanh", 32000, 90, 500, "Thanh Hóa", "dau-va-hat",
                        "dau-xanh-1.webp"),
                new SeedProduct("Hạt chia", "hat-chia", 95000, 50, 250, "Đà Lạt", "dau-va-hat",
                        "hat-chia-1.webp"),
                new SeedProduct("Hạt điều rang muối", "hat-dieu-rang-muoi", 88000, 45, 500, "Bình Phước", "dau-va-hat",
                        "hat-dieu-rang-muoi-1.webp"),
                new SeedProduct("Hạnh nhân Mỹ", "hanh-nhan-my", 110000, 38, 400, "Mỹ", "dau-va-hat",
                        "hanh-nhan-my-1.webp",
                        "hanh-nhan-my-2.webp"),
                new SeedProduct("Óc chó Mỹ", "oc-cho-my", 125000, 32, 400, "Mỹ", "dau-va-hat",
                        "oc-cho-my-1.webp",
                        "oc-cho-my-2.webp"),
                new SeedProduct("Đậu đỏ Tây Ninh", "dau-do-tay-ninh", 30000, 75, 500, "Tây Ninh", "dau-va-hat",
                        "dau-do-tay-ninh-1.webp",
                        "dau-do-tay-ninh-2.webp"),
                // Trái cây sấy
                new SeedProduct("Xoài sấy dẻo", "xoai-say-deo", 65000, 60, 250, "Tiền Giang", "trai-cay-say",
                        "xoai-say-deo-1.webp",
                        "xoai-say-deo-2.webp"),
                new SeedProduct("Chuối sấy giòn", "chuoi-say-gion", 45000, 80, 250, "Tiền Giang", "trai-cay-say",
                        "chuoi-say-gion-1.webp",
                        "chuoi-say-gion-2.webp"),
                new SeedProduct("Mít sấy giòn", "mit-say-gion", 55000, 70, 250, "Tiền Giang", "trai-cay-say",
                        "mit-say-gion-1.webp",
                        "mit-say-gion-2.webp")
        );
    }

    private void seedProducts() {
        for (SeedProduct sp : seedProductList()) {
            seedProduct(sp);
        }
    }

    private void seedProduct(SeedProduct sp) {
        if (productRepository.existsBySlug(sp.slug())) {
            return;
        }
        Category category = categoryRepository.findBySlug(sp.categorySlug()).orElse(null);
        Product p = new Product();
        p.setName(sp.name());
        p.setSlug(sp.slug());
        p.setDescription("Sản phẩm " + sp.name() + " tươi ngon, đóng gói an toàn vệ sinh thực phẩm. "
                + "Nguồn gốc rõ ràng từ " + sp.origin() + ".");
        p.setPrice(sp.price());
        p.setStock(sp.stock());
        p.setWeight(sp.weight());
        p.setOrigin(sp.origin());
        p.setCategory(category);
        p.setActive(true);

        int order = 0;
        for (String fileName : sp.images()) {
            ProductImage img = new ProductImage();
            img.setProduct(p);
            img.setUrl("/images/products/" + fileName);
            img.setPrimary(order == 0);
            img.setDisplayOrder(order);
            p.getImages().add(img);
            order++;
        }

        p = productRepository.save(p);

        Material material = materialRepository.findByName(materialForCategory(sp.categorySlug()))
                .orElseGet(() -> materialRepository.findAll().stream().findFirst().orElse(null));
        if (material != null) {
            ProductMaterial pm = new ProductMaterial();
            pm.setId(new ProductMaterialId(p.getId(), material.getId()));
            pm.setProduct(p);
            pm.setMaterial(material);
            pm.setPercentage(100);
            p.getMaterials().add(pm);
            productRepository.save(p);
        }
    }

    private String materialForCategory(String categorySlug) {
        return switch (categorySlug) {
            case "rau-xanh" -> "Lá chuối";
            case "cu-qua", "ngu-coc" -> "Giấy";
            case "trai-cay-nhiet-doi" -> "Mía / bã mía";
            case "trai-cay-nhap-khau" -> "Hộp nhựa";
            case "dau-va-hat" -> "Hộp nhựa";
            case "trai-cay-say" -> "Túi vải";
            default -> "Giấy";
        };
    }

    // ------------------------------------------------------------ addresses

    private record SeedAddress(String label, String street, String ward, String district, String city,
                               String receiverName, String receiverPhone) {}

    private void seedAddresses() {
        List<Customer> customers = demoCustomers();
        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            List<Address> existing = addressRepository.findByCustomerId(c.getId());
            if (!existing.isEmpty() && existing.size() >= addressCountFor(i)) {
                continue;
            }
            for (SeedAddress sa : addressesFor(i)) {
                Address a = new Address();
                a.setCustomer(c);
                a.setLabel(sa.label());
                a.setStreet(sa.street());
                a.setWard(sa.ward());
                a.setDistrict(sa.district());
                a.setCity(sa.city());
                a.setReceiverName(sa.receiverName());
                a.setReceiverPhone(sa.receiverPhone());
                a.setDefault(false);
                addressRepository.save(a);
            }
            List<Address> saved = addressRepository.findByCustomerId(c.getId());
            if (!saved.isEmpty()) {
                saved.get(0).setDefault(true);
                addressRepository.save(saved.get(0));
            }
        }
    }

    private List<Customer> demoCustomers() {
        List<Customer> customers = new ArrayList<>();
        for (Customer c : customerRepository.findAll()) {
            if (c.getUsername().matches("customer\\d?")) {
                customers.add(c);
            }
        }
        customers.sort((a, b) -> Integer.compare(customerIndex(a.getUsername()), customerIndex(b.getUsername())));
        return customers;
    }

    private int customerIndex(String username) {
        try {
            return username.length() == 8 ? 1 : Integer.parseInt(username.substring(8));
        } catch (NumberFormatException e) {
            return 100;
        }
    }

    private int addressCountFor(int i) {
        return i == 0 ? 1 : 2;
    }

    private List<SeedAddress> addressesFor(int i) {
        String[][] raw = {
                {"Nhà", "12 Lê Lợi", "Phường Bến Nghé", "Quận 1", "TP.HCM"},
                {"Cơ quan", "45 Trần Hưng Đạo", "Phường Cầu Ông Lãnh", "Quận 1", "TP.HCM"},
                {"Nhà", "78 Đặng Văn Ngữ", "Phường Trung Tự", "Đống Đa", "Hà Nội"},
                {"Nhà", "23 Bạch Đằng", "Phường Hải Châu 1", "Hải Châu", "Đà Nẵng"},
                {"Nhà", "90 Hòa Bình", "Phường An Cư", "Ninh Kiều", "Cần Thơ"},
                {"Nhà", "15 Nguyễn Trãi", "Phường Quang Trung", "Vinh", "Nghệ An"},
                {"Nhà", "67 Phú Lợi", "Phường Phú Lợi", "Thủ Dầu Một", "Bình Dương"},
                {"Nhà", "33 Võ Văn Tần", "Phường 6", "Quận 3", "TP.HCM"},
        };
        List<SeedAddress> list = new ArrayList<>();
        String[] base = raw[i % raw.length];
        list.add(new SeedAddress("Nhà", base[1], base[2], base[3], base[4],
                receiverName(i), receiverPhone(i)));
        if (i != 0) {
            list.add(new SeedAddress("Cơ quan", "123 " + base[1], base[2], base[3], base[4],
                    receiverName(i), receiverPhone(i)));
        }
        return list;
    }

    private String receiverName(int i) {
        String[] names = {"Nguyễn Văn An", "Trần Thu Hà", "Lê Minh Khôi", "Phạm Ngọc Bích",
                "Hoàng Đức Long", "Vũ Thị Lan", "Đặng Quang Huy", "Bùi Thanh Mai"};
        return names[i % names.length];
    }

    private String receiverPhone(int i) {
        return "09" + (800000000 + i);
    }

    // ------------------------------------------------------------ cart items

    private void seedCartItems() {
        List<Customer> customers = demoCustomers();
        for (int i = 1; i < customers.size(); i++) {
            Customer c = customers.get(i);
            Cart cart = c.getCart();
            if (cart == null || !cart.getItems().isEmpty()) {
                continue;
            }
            List<Product> inStock = productRepository.findAll().stream()
                    .filter(p -> p.isActive() && p.getStock() > 0)
                    .toList();
            if (inStock.isEmpty()) {
                continue;
            }
            int itemCount = Math.min(2 + i % 4, inStock.size());
            Random rnd = new Random(1000L + i);
            java.util.Set<Long> picked = new java.util.HashSet<>();
            int attempts = 0;
            while (picked.size() < itemCount && attempts < 100) {
                attempts++;
                Product p = inStock.get(rnd.nextInt(inStock.size()));
                if (!picked.add(p.getId())) {
                    continue;
                }
                CartItem ci = new CartItem();
                ci.setId(new CartItemId(cart.getId(), p.getId()));
                ci.setCart(cart);
                ci.setProduct(p);
                ci.setQuantity(1 + rnd.nextInt(4));
                cartItemRepository.save(ci);
            }
        }
    }

    // --------------------------------------------------------------- orders

    private record OrderSpec(List<String> productSlugs, OrderStatus status, PaymentMethod method) {}

    private void seedOrders() {
        List<Customer> customers = demoCustomers().stream()
                .filter(c -> !c.getUsername().equals("customer"))
                .toList();
        if (customers.isEmpty()) {
            return;
        }
        long existing = customers.stream()
                .mapToLong(c -> orderRepository.countByCustomerId(c.getId()))
                .sum();
        if (existing > 0) {
            return;
        }
        List<OrderSpec> specs = orderSpecs();
        if (specs.isEmpty()) {
            return;
        }
        int cIdx = 0;
        for (OrderSpec spec : specs) {
            Customer c = customers.get(cIdx % customers.size());
            cIdx++;
            seedOrder(c, spec);
        }
    }

    private void seedOrder(Customer customer, OrderSpec spec) {
        if (orderRepository.countByCustomerId(customer.getId()) >= 20) {
            return;
        }
        List<Address> addresses = addressRepository.findByCustomerId(customer.getId());
        if (addresses.isEmpty()) {
            return;
        }
        Address addr = addresses.get(0);
        List<Product> products = new ArrayList<>();
        for (String slug : spec.productSlugs()) {
            productRepository.findBySlug(slug).ifPresent(products::add);
        }
        if (products.isEmpty()) {
            return;
        }
        double subtotal = 0;
        List<int[]> qty = new ArrayList<>();
        Random rnd = new Random(5000L + customer.getId());
        for (Product p : products) {
            int q = 1 + rnd.nextInt(3);
            qty.add(new int[]{products.indexOf(p), q});
            subtotal += p.getPrice() * q;
        }
        double fee = shopProperties.shippingFee();
        double total = subtotal + fee;

        Order order = new Order();
        order.setCustomer(customer);
        order.setReceiverName(addr.getReceiverName());
        order.setReceiverPhone(addr.getReceiverPhone());
        order.setAddress(addr.getStreet() + ", " + addr.getWard() + ", " + addr.getDistrict() + ", " + addr.getCity());
        order.setStatus(spec.status());
        order.setSubtotal(subtotal);
        order.setShippingFee(fee);
        order.setTotal(total);
        order.setNotes(rnd.nextInt(3) == 0 ? "Giao giờ hành chính" : null);
        int daysAgo = 1 + rnd.nextInt(28);
        order.setCreatedAt(Instant.now().minus(daysAgo, ChronoUnit.DAYS).minus(rnd.nextInt(86400), ChronoUnit.SECONDS));
        order = orderRepository.save(order);

        for (int[] entry : qty) {
            Product p = products.get(entry[0]);
            OrderItem oi = new OrderItem();
            oi.setId(new OrderItemId(order.getId(), p.getId()));
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setProductNameSnapshot(p.getName());
            oi.setQuantity(entry[1]);
            oi.setUnitPrice(p.getPrice());
            orderItemRepository.save(oi);
            order.getItems().add(oi);
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(spec.method());
        payment.setStatus(paymentStatusFor(spec));
        payment.setAmount(total);
        if (spec.method() == PaymentMethod.PAYOS) {
            payment.setPayosOrderCode(String.valueOf(order.getId()));
        }
        if (payment.getStatus() == PaymentStatus.PAID) {
            payment.setPaidAt(order.getCreatedAt().plus(2, ChronoUnit.HOURS));
        }
        paymentRepository.save(payment);
        order.setPayment(payment);
        orderRepository.save(order);

        notificationRepository.save(notification(customer,
                "Đơn hàng #" + order.getId() + " đã được tạo",
                "Đơn hàng của bạn với tổng giá trị " + Math.round(total) + "đ đã được ghi nhận.",
                String.valueOf(order.getId()),
                order.getCreatedAt()));
        if (payment.getStatus() == PaymentStatus.PAID) {
            notificationRepository.save(notification(customer,
                    "Thanh toán đơn hàng #" + order.getId() + " thành công",
                    "Cảm ơn bạn! Thanh toán cho đơn hàng đã được hoàn tất.",
                    String.valueOf(order.getId()),
                    order.getCreatedAt().plus(3, ChronoUnit.HOURS)));
        }
    }

    private PaymentStatus paymentStatusFor(OrderSpec spec) {
        return switch (spec.status()) {
            case COMPLETED -> PaymentStatus.PAID;
            case SHIPPING -> spec.method() == PaymentMethod.PAYOS ? PaymentStatus.PAID : PaymentStatus.PENDING;
            case CANCELLED -> spec.method() == PaymentMethod.PAYOS ? PaymentStatus.FAILED : PaymentStatus.CANCELLED;
            default -> PaymentStatus.PENDING;
        };
    }

    private Notification notification(User user, String title, String message, String referenceId, Instant createdAt) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(NotificationType.ORDER);
        n.setReferenceId(referenceId);
        n.setCreatedAt(createdAt);
        return n;
    }

    private List<OrderSpec> orderSpecs() {
        List<Product> products = productRepository.findAll().stream()
                .filter(p -> p.isActive() && p.getStock() > 0)
                .toList();
        int n = products.size();
        if (n < 10) {
            return List.of();
        }
        List<String> slugs = products.stream().map(Product::getSlug).toList();
        // ~30 đơn, xoay vòng đủ 5 trạng thái + 2 phương thức, mỗi đơn 1-4 sản phẩm khác nhau
        List<OrderSpec> specs = new ArrayList<>();
        OrderStatus[] statuses = {OrderStatus.COMPLETED, OrderStatus.SHIPPING, OrderStatus.PENDING,
                OrderStatus.CONFIRMED, OrderStatus.CANCELLED};
        PaymentMethod[] methods = {PaymentMethod.PAYOS, PaymentMethod.COD};
        int base = 0;
        for (int i = 0; i < 30; i++) {
            OrderStatus st = statuses[i % statuses.length];
            PaymentMethod m = methods[(i * 7 + 3) % methods.length];
            int itemCount = 1 + i % 4;
            List<String> picked = new ArrayList<>();
            int offset = base;
            for (int k = 0; k < itemCount; k++) {
                picked.add(slugs.get(offset % n));
                offset += 3;
            }
            specs.add(new OrderSpec(picked, st, m));
            base += 2;
        }
        return specs;
    }

    // -------------------------------------------------------------- reviews

    private void seedReviews() {
        List<Customer> customers = demoCustomers().stream()
                .filter(c -> !c.getUsername().equals("customer"))
                .toList();
        if (customers.isEmpty()) {
            return;
        }
        String[][] templates = {
                {"4", "Sản phẩm tươi, chất lượng tốt. Đóng gói cẩn thận và giao nhanh."},
                {"5", "Rất hài lòng với chất lượng. Giá cả hợp lý cho sản phẩm sạch như thế này."},
                {"4", "Đóng gói bao bì thân thiện môi trường, đúng cam kết xanh của shop."},
                {"5", "Tươi ngon, đúng nguồn gốc. Sẽ mua lại lần sau."},
                {"3", "Sản phẩm ổn, nhưng lần này giao hơi chậm một chút."},
                {"5", "Chất lượng tốt, ship nhanh, đóng gói kỹ. 10 điểm."},
                {"4", "Hàng ngon, giá hơi cao nhưng xứng đáng với sản phẩm sạch."},
                {"2", "Lần này hàng không được tươi như mọi khi, hy vọng shop cải thiện."},
        };
        List<Product> products = productRepository.findAll().stream()
                .filter(Product::isActive)
                .toList();
        Random rnd = new Random(777L);
        for (int pi = 0; pi < products.size(); pi++) {
            Product p = products.get(pi);
            for (int k = 0; k < 3; k++) {
                Customer c = customers.get((pi * 2 + k) % customers.size());
                if (reviewRepository.existsByCustomerIdAndProductId(c.getId(), p.getId())) {
                    continue;
                }
                String[] t = templates[rnd.nextInt(templates.length)];
                Review r = new Review();
                r.setCustomer(c);
                r.setProduct(p);
                r.setRating(Integer.parseInt(t[0]));
                r.setContent(t[1]);
                r.setHidden(false);
                r.setCreatedAt(Instant.now().minus(rnd.nextInt(27) + 1, ChronoUnit.DAYS));
                reviewRepository.save(r);
            }
        }
    }
}
