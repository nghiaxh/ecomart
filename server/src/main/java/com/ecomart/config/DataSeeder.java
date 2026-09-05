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
        banner("Ưu đãi cuối tuần", "Giảm giá nhiều mặt hàng thiết yếu hàng ngày", "https://images.unsplash.com/photo-1542838132-92c53300491e?w=1600", "/products?category=rau-cu-sach", 1);
        banner("Hàng mới về", "Khám phá bộ sưu tập sản phẩm mới nhất", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=1600", "/products", 2);
        banner("Trái cây tươi mỗi ngày", "Chọn lọc từ những vùng trồng uy tín", "https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=1600", "/products?category=trai-cay-tuoi", 3);
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

    private void seedProducts() {
        List<SeedProduct> products = List.of(
                // Rau xanh
                new SeedProduct("Rau muống sạch", "rau-muong-sach", 12000, 100, 200, "Đà Lạt", "rau-xanh",
                        "https://images.unsplash.com/photo-1542838132-92c53300491e?w=800"),
                new SeedProduct("Cải bó xôi", "cai-bo-xoi", 18000, 80, 150, "Đà Lạt", "rau-xanh",
                        "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=800"),
                new SeedProduct("Xà lách xoăn", "xa-lach-xoan", 22000, 60, 150, "Đà Lạt", "rau-xanh",
                        "https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?w=800"),
                new SeedProduct("Cải thảo", "cai-thao", 16000, 90, 300, "Mộc Châu", "rau-xanh",
                        "https://images.unsplash.com/photo-1466637574441-749b8f19452f?w=800"),
                new SeedProduct("Rau ngót", "rau-ngot", 15000, 70, 200, "Lâm Đồng", "rau-xanh",
                        "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800"),
                // Củ quả
                new SeedProduct("Cà rốt Đà Lạt", "ca-rot-da-lat", 15000, 120, 250, "Đà Lạt", "cu-qua",
                        "https://images.unsplash.com/photo-1447175008436-054170c2e979?w=800"),
                new SeedProduct("Khoai tây", "khoai-tay", 16000, 90, 300, "Lâm Đồng", "cu-qua",
                        "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=800"),
                new SeedProduct("Bí đỏ", "bi-do", 25000, 50, 1200, "Đà Lạt", "cu-qua",
                        "https://images.unsplash.com/photo-1553621042-f6e147245754?w=800"),
                new SeedProduct("Cà chua sạch", "ca-chua-sach", 28000, 80, 500, "Lâm Đồng", "cu-qua",
                        "https://images.unsplash.com/photo-1561136594-7f68413baa99?w=800"),
                new SeedProduct("Dưa chuột", "dua-chuot", 12000, 100, 300, "Lâm Đồng", "cu-qua",
                        "https://images.unsplash.com/photo-1438593793753-34b209e3281b?w=800"),
                new SeedProduct("Hành tây", "hanh-tay", 18000, 90, 250, "Ninh Thuận", "cu-qua",
                        "https://images.unsplash.com/photo-1508747703725-719777637510?w=800"),
                new SeedProduct("Ớt chuông", "ot-chuong", 32000, 60, 200, "Lâm Đồng", "cu-qua",
                        "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=800"),
                // Trái cây nhiệt đới
                new SeedProduct("Cam sành Việt", "cam-sanh-viet", 25000, 60, 300, "Tây Ninh", "trai-cay-nhiet-doi",
                        "https://images.unsplash.com/photo-1547514701-42782101795e?w=800"),
                new SeedProduct("Xoài cát Hòa Lộc", "xoai-cat-hoa-loc", 45000, 50, 500, "Tiền Giang", "trai-cay-nhiet-doi",
                        "https://images.unsplash.com/photo-1553279768-865429fa0078?w=800"),
                new SeedProduct("Chuối sứ", "chuoi-su", 18000, 150, 400, "Tiền Giang", "trai-cay-nhiet-doi",
                        "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=800"),
                new SeedProduct("Bưởi da xanh", "buoi-da-xanh", 35000, 40, 1300, "Bến Tre", "trai-cay-nhiet-doi",
                        "https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=800"),
                new SeedProduct("Dưa hấu ruột đỏ", "dua-hau-ruot-do", 30000, 30, 2500, "Long An", "trai-cay-nhiet-doi",
                        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=800"),
                // Trái cây nhập khẩu
                new SeedProduct("Táo Mỹ", "tao-my", 55000, 70, 200, "Mỹ", "trai-cay-nhap-khau",
                        "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=800"),
                new SeedProduct("Nho xanh không hạt", "nho-xanh-khong-hat", 60000, 60, 500, "Úc", "trai-cay-nhap-khau",
                        "https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=800"),
                // Ngũ cốc
                new SeedProduct("Gạo lứt hữu cơ", "gao-lut-huu-co", 68000, 70, 1000, "An Giang", "ngu-coc",
                        "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=800"),
                new SeedProduct("Gạo ST25", "gao-st25", 89000, 60, 1000, "Sóc Trăng", "ngu-coc",
                        "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?w=800"),
                new SeedProduct("Yến mạch nguyên chất", "yen-mach-nguyen-chat", 45000, 80, 500, "Bắc Giang", "ngu-coc",
                        "https://images.unsplash.com/photo-1517673400267-0251440c45dc?w=800"),
                // Đậu & hạt
                new SeedProduct("Đậu xanh", "dau-xanh", 32000, 90, 500, "Thanh Hóa", "dau-va-hat",
                        "https://images.unsplash.com/photo-1515543904379-3d757afe72e4?w=800"),
                new SeedProduct("Hạt chia", "hat-chia", 95000, 50, 250, "Đà Lạt", "dau-va-hat",
                        "https://images.unsplash.com/photo-1511690656952-34342bb7c2f2?w=800"),
                new SeedProduct("Hạt điều rang muối", "hat-dieu-rang-muoi", 88000, 45, 500, "Bình Phước", "dau-va-hat",
                        "https://images.unsplash.com/photo-1606923829579-0cb981a83e2e?w=800")
        );

        for (SeedProduct sp : products) {
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
        for (String url : sp.images()) {
            ProductImage img = new ProductImage();
            img.setProduct(p);
            img.setUrl(url);
            img.setPrimary(order == 0);
            img.setDisplayOrder(order);
            p.getImages().add(img);
            order++;
        }

        List<Material> materials = materialRepository.findAll();
        if (!materials.isEmpty()) {
            ProductMaterial pm = new ProductMaterial();
            pm.setId(new ProductMaterialId(0L, materials.get(0).getId()));
            pm.setProduct(p);
            pm.setMaterial(materials.get(0));
            pm.setPercentage(100);
            p.getMaterials().add(pm);
        }
        productRepository.save(p);
    }
}