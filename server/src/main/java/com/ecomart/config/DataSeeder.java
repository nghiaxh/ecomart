package com.ecomart.config;

import com.ecomart.domain.entity.*;
import com.ecomart.domain.enums.MaterialType;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final CategoryRepository categoryRepository;
    private final MaterialRepository materialRepository;
    private final ProductRepository productRepository;
    private final BannerRepository bannerRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.admin-password:}")
    private String seedAdminPassword;

    @Value("${app.seed.customer-password:}")
    private String seedCustomerPassword;

    public DataSeeder(UserRepository userRepository,
                      CustomerRepository customerRepository,
                      CartRepository cartRepository,
                      CategoryRepository categoryRepository,
                      MaterialRepository materialRepository,
                      ProductRepository productRepository,
                      BannerRepository bannerRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.cartRepository = cartRepository;
        this.categoryRepository = categoryRepository;
        this.materialRepository = materialRepository;
        this.productRepository = productRepository;
        this.bannerRepository = bannerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedDemoUsers();
        seedCategories();
        seedMaterials();
        seedBanners();
        seedProducts();
    }

    private void seedDemoUsers() {
        seedAdmin();
        seedCustomer();
    }

    private void seedAdmin() {
        if (!seedEnabled || userRepository.existsByUsername("admin")) {
            return;
        }
        String password = seedAdminPassword != null && !seedAdminPassword.isBlank()
                ? seedAdminPassword : "Admin@123";
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setEmail("admin@ecomart.vn");
        admin.setNumberPhone("0900000000");
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        admin.setHireDate(java.time.LocalDate.now());
        userRepository.save(admin);
    }

    private void seedCustomer() {
        if (!seedEnabled || userRepository.existsByUsername("customer")) {
            return;
        }
        String password = seedCustomerPassword != null && !seedCustomerPassword.isBlank()
                ? seedCustomerPassword : "Customer@123";
        Customer customer = new Customer();
        customer.setUsername("customer");
        customer.setEmail("customer@ecomart.vn");
        customer.setNumberPhone("0901111111");
        customer.setPasswordHash(passwordEncoder.encode(password));
        customer.setRole(UserRole.CUSTOMER);
        customer.setActive(true);
        customer = customerRepository.save(customer);

        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart = cartRepository.save(cart);
        customer.setCart(cart);
        customerRepository.save(customer);
    }

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

    private void seedBanners() {
        banner("Ưu đãi cuối tuần", "Giảm giá nhiều mặt hàng thiết yếu hàng ngày", "/images/banners/banner-1-uu-dai.webp", "/products?category=rau-cu-sach", 1);
        banner("Hàng mới về", "Khám phá bộ sưu tập sản phẩm mới nhất", "/images/banners/banner-2-hang-moi.webp", "/products", 2);
        banner("Trái cây tươi mỗi ngày", "Chọn lọc từ những vùng trồng uy tín", "/images/banners/banner-3-trai-cay.webp", "/products?category=trai-cay-tuoi", 3);
    }

    private void banner(String title, String subtitle, String image, String link, int order) {
        bannerRepository.findByTitle(title).orElseGet(() -> {
            Banner b = new Banner();
            b.setTitle(title);
            b.setSubtitle(subtitle);
            b.setImageUrl(image);
            b.setLinkUrl(link);
            b.setDisplayOrder(order);
            b.setActive(true);
            return bannerRepository.save(b);
        });
    }

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
}