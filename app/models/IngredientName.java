package models;

import play.db.ebean.Model;

import javax.persistence.*;

@Entity
public class IngredientName extends Model {
    @Id
    private long id;

    @Column(length = 1024, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="ingredient_id", referencedColumnName="id")
    private Ingredient ingredient;

    public static Finder<Long, IngredientName> find = new Finder<>(Long.class, IngredientName.class);
}
