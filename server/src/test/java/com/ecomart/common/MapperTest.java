package com.ecomart.common;

import com.ecomart.domain.entity.Category;
import com.ecomart.domain.entity.Material;
import com.ecomart.domain.entity.Product;
import com.ecomart.domain.entity.ProductImage;
import com.ecomart.domain.entity.ProductMaterial;
import com.ecomart.dto.response.CategoryResponse;
import com.ecomart.dto.response.ProductResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapperTest {

    @Test
    void toProductSortsImagesByDisplayOrder() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Bơ");
        product.setSlug("bo");
        product.setPrice(25000);
        product.setStock(10);
        product.setActive(true);

        ProductImage first = new ProductImage();
        first.setUrl("/images/2.webp");
        first.setDisplayOrder(2);
        ProductImage second = new ProductImage();
        second.setUrl("/images/0.webp");
        second.setDisplayOrder(0);
        ProductImage third = new ProductImage();
        third.setUrl("/images/1.webp");
        third.setDisplayOrder(1);
        product.getImages().addAll(List.of(first, second, third));

        Material material = new Material();
        material.setId(10L);
        material.setName("Hữu cơ");
        ProductMaterial pm = new ProductMaterial();
        pm.setMaterial(material);
        pm.setPercentage(100.0);
        product.getMaterials().add(pm);

        ProductResponse response = Mapper.toProduct(product);

        assertEquals(List.of("/images/0.webp", "/images/1.webp", "/images/2.webp"), response.images());
        assertEquals("Hữu cơ", response.materials().get(0).name());
    }

    @Test
    void toCategoryBuildsNestedChildrenRecursively() {
        Category grandchild = category(3L, "Cháu");
        Category child = category(2L, "Trái cây");
        child.setParent(category(1L, "Cha"));
        child.getChildren().add(grandchild);
        Category root = category(1L, "Thực phẩm");
        root.getChildren().add(child);

        CategoryResponse response = Mapper.toCategory(root);

        assertEquals("Thực phẩm", response.name());
        assertEquals(1, response.children().size());
        assertEquals("Trái cây", response.children().get(0).name());
        assertEquals("Cháu", response.children().get(0).children().get(0).name());
    }

    private Category category(long id, String name) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        c.setSlug("slug-" + id);
        c.setDisplayOrder(1);
        c.setActive(true);
        return c;
    }
}