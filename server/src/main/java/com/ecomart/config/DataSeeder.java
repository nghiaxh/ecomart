package com.ecomart.config;

import com.ecomart.domain.entity.*;
import com.ecomart.domain.enums.MaterialType;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MaterialRepository materialRepository;
    private final ProductRepository productRepository;
    private final BannerRepository bannerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      MaterialRepository materialRepository,
                      ProductRepository productRepository,
                      BannerRepository bannerRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.materialRepository = materialRepository;
        this.productRepository = productRepository;
        this.bannerRepository = bannerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        seedAdmin();
        seedCategories();
        seedMaterials();
        seedBanners();
        seedProducts();
    }

    private void seedAdmin() {
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setEmail("admin@ecomart.vn");
        admin.setNumberPhone("0900000000");
        admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        admin.setHireDate(java.time.LocalDate.now());
        userRepository.save(admin);
    }

    private void seedCategories() {
        Category rau = category("Rau củ sạch", "rau-cu-sach", "leaf", 1);
        Category trai = category("Trái cây tươi", "trai-cay-tuoi", "apple", 2);
        Category hangKho = category("Thực phẩm khô", "thuc-pham-kho", "box", 3);

        Category rauXanh = category("Rau xanh", "rau-xanh", null, 1);
        rauXanh.setParent(rau);
        saveParent(rauXanh);

        Category cuQua = category("Củ quả", "cu-qua", null, 2);
        cuQua.setParent(rau);
        saveParent(cuQua);

        Category traiNhietDoi = category("Trái cây nhiệt đới", "trai-cay-nhiet-doi", null, 1);
        traiNhietDoi.setParent(trai);
        saveParent(traiNhietDoi);

        Category nguCoc = category("Ngũ cốc", "ngu-coc", null, 1);
        nguCoc.setParent(hangKho);
        saveParent(nguCoc);
    }

    private void saveParent(Category child) {
        child.getParent().getChildren().add(child);
        categoryRepository.save(child.getParent());
    }

    private Category category(String name, String slug, String icon, int order) {
        Category c = new Category();
        c.setName(name);
        c.setSlug(slug);
        c.setIcon(icon);
        c.setDisplayOrder(order);
        c.setActive(true);
        return categoryRepository.save(c);
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
        Material m = new Material();
        m.setName(name);
        m.setType(type);
        materialRepository.save(m);
    }

    private void seedBanners() {
        banner("Ưu đãi cuối tuần", "Giảm giá nhiều mặt hàng thiết yếu hàng ngày", "https://images.unsplash.com/photo-1542838132-92c53300491e?w=1600", "/products?category=rau-cu-sach", 1);
        banner("Hàng mới về", "Khám phá bộ sưu tập sản phẩm mới nhất", "https://images.unsplash.com/photo-1542838132-92c53300491e?w=1600", "/products", 2);
    }

    private void banner(String title, String subtitle, String image, String link, int order) {
        Banner b = new Banner();
        b.setTitle(title);
        b.setSubtitle(subtitle);
        b.setImageUrl(image);
        b.setLinkUrl(link);
        b.setDisplayOrder(order);
        b.setActive(true);
        bannerRepository.save(b);
    }

    private void seedProducts() {
        Category rauXanh = categoryRepository.findBySlug("rau-xanh").orElse(null);
        product("Rau muống sạch", "rau-muong-sach", 12000, 100, rauXanh, 200, "Đà Lạt");
        product("Cải bó xôi", "cai-bo-xoi", 18000, 80, rauXanh, 150, "Đà Lạt");
        Category qua = categoryRepository.findBySlug("trai-cay-nhiet-doi").orElse(null);
        product("Cam sành Việt", "cam-sanh-viet", 25000, 60, qua, 300, "Tây Ninh");
        product("Xoài cát Hòa Lộc", "xoai-cat-hoa-loc", 45000, 50, qua, 500, "Tiền Giang");
    }

    private void product(String name, String slug, double price, int stock,
                         Category category, double weight, String origin) {
        if (productRepository.existsBySlug(slug)) {
            return;
        }
        Product p = new Product();
        p.setName(name);
        p.setSlug(slug);
        p.setDescription("Sản phẩm " + name + " tươi ngon, đóng gói an toàn vệ sinh thực phẩm.");
        p.setPrice(price);
        p.setStock(stock);
        p.setWeight(weight);
        p.setOrigin(origin);
        p.setCategory(category);
        p.setActive(true);

        ProductImage img = new ProductImage();
        img.setProduct(p);
        img.setUrl("https://images.unsplash.com/photo-1542838132-92c53300491e?w=800");
        img.setPrimary(true);
        img.setDisplayOrder(0);
        p.getImages().add(img);

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
