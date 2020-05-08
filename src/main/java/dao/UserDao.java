package dao;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import core.user.Profile;
import core.user.User;

@RequestScoped
public class UserDao {

    @PersistenceContext(name = "realWorld-jpa")
    private EntityManager em;

    public void createUser(User user) {
        try {
            em.persist(user);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteUser(Long userId) {
        em.remove(em.find(User.class, userId));
    }

    public User updateUser(User user, User newUser) {
        if (user == null) return null;
        user.update(
            newUser.getEmail(), 
            newUser.getUsername(), 
            newUser.getPassword(), 
            newUser.getImg(), 
            newUser.getBio());
        return em.merge(user);
    }

    public User findUser(Long userId) {
        try {
            return (userId == null) ? null: em.find(User.class, userId);
        } catch (NoResultException e) {
            return null;
        }
    }

    public Profile findProfile(String username) {
        try {
            return em.createQuery("SELECT p FROM Profile p WHERE p.username = :username", Profile.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User login(String email, String password) {
        return em.createQuery("SELECT u FROM User u WHERE u.email = :email AND u.password = :password", User.class)
                .setParameter("email", email)
                .setParameter("password", password)
                .getSingleResult();
    }

    // JPA count returns type Long
    public boolean userExists(String username) {
        return (Long) em.createQuery("SELECT COUNT(u.USER_ID) FROM User u WHERE u.username = :username")
                .setParameter("username", username)
                .getSingleResult() > 0;
    }

    public boolean emailExists(String email) {
        return (Long) em.createQuery("SELECT COUNT(u.USER_ID) FROM User u WHERE u.email = :email")
                .setParameter("email", email)
                .getSingleResult() > 0;
    }
}
