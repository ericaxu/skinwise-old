package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Ingredient extends Model {
    @Id
    private long id;

    @Column(length = 1024, unique = true)
    private String name;

    @Column(length = 128, unique = true)
    private String cas_number;

    @Column(length = 1024)
    private String short_desc;

    @OneToMany(mappedBy = "ingredient", fetch = FetchType.LAZY)
    List<IngredientName> names = new ArrayList<>();

    @OneToMany(mappedBy = "ingredient", fetch = FetchType.LAZY)
    List<IngredientFunction> functions = new ArrayList<>();

    public static Finder<Long, Ingredient> find = new Finder<>(Long.class, Ingredient.class);
}
