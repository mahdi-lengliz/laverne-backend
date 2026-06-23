package nst.laverne.lavernebackend.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "subtitle")
    private String sub;

    private Integer perfumeSize;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private String emoji;
    private String badge;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Integer stock = 0;

    private String imageUrl;
    private String imageUrl2;
    private String imageUrl3;
    private String imageUrl4;

    @Column(nullable = false)
    private boolean collection;

    @ElementCollection
    @CollectionTable(name = "product_notes", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "note")
    private List<String> notes = new ArrayList<>();
}
