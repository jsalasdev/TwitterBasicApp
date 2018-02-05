package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.Entity;

@Entity
public class Follower extends Model {

    public String name;
    public String email;
    public String date;
    public Integer postalCode;

    public static Finder<Integer, Follower> find = new Finder<>(Follower.class);

}
