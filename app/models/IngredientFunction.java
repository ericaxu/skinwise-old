package models;

import play.db.ebean.Model;

import javax.persistence.*;

@Entity
public class IngredientFunction extends Model {
    @Id
    private long id;

    @Column(length = 256, unique = true)
    private String function;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="ingredient_id", referencedColumnName="id")
    private Ingredient ingredient;

    public static Finder<Long, IngredientFunction> find = new Finder<>(Long.class, IngredientFunction.class);
}
