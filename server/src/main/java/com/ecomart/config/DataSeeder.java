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
        material("Giấy tái chế", 0.6, MaterialType.RECYCLED);
        material("Nhựa tái chế (rPET)", 2.1, MaterialType.RECYCLED);
        material("Tre", 0.3, MaterialType.NATURAL);
        material("Thủy tinh", 0.8, MaterialType.RECYCLED);
        material("Bông hữu cơ", 1.2, MaterialType.ORGANIC);
        material("Mía / bã mía", 0.4, MaterialType.ORGANIC);
        material("Lá chuối", 0.1, MaterialType.ORGANIC);
    }

    private void material(String name, double index, MaterialType type) {
        Material m = new Material();
        m.setName(name);
        m.setEmissionIndex(index);
        m.setType(type);
        materialRepository.save(m);
    }

    private void seedBanners() {
        banner("Sống xanh mỗi ngày", "Ưu đãi đặc biệt cho sản phẩm hữu cơ", "https://images.unsplash.com/photo-1542838132-92c53300491e?w=1600", "/products?category=rau-cu-sach", 1);
        banner("Tiêu dùng bền vững", "Khám phá bộ sưu tập thân thiện môi trường", "https://images.unsplash.com/photo-1542838132-92c53300491e?w=1600", "/products", 2);
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
        product("Rau muống hữu cơ", "rau-muong-huu-co", 12000, 100, 2.0, 0.8, 5, rauXanh, 200, "Đà Lạt");
        product("Cải bó xôi sạch", "cai-bo-xoi-sach", 18000, 80, 2.4, 1.0, 6, rauXanh, 150, "Đà Lạt");
        Category quả = categoryRepository.findBySlug("trai-cay-nhiet-doi").orElse(null);
        product("Cam sành Việt", "cam-sanh-viet", 25000, 60, 1.8, 0.7, 4, quả, 300, "Tây Ninh");
    }

    private void product(String name, String slug, double price, int stock,
                         double carbonIndex, double baseline, double ecoPoints,
                         Category category, double weight, String origin) {
        if (productRepository.existsBySlug(slug)) {
            return;
        }
        Product p = new Product();
        p.setName(name);
        p.setSlug(slug);
        p.setDescription("Sản phẩm " + name + " được trồng hữu cơ, thân thiện với môi trường.");
        p.setPrice(price);
        p.setStock(stock);
        p.setCarbonIndex(carbonIndex);
        p.setBaselineCarbonIndex(baseline);
        p.setEcoPointsPerUnit(ecoPoints);
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
