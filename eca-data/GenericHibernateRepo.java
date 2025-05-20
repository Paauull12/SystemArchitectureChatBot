package ro.mpp2025.hibernaterepo;

import org.hibernate.Session;
import ro.mpp2025.Entity;
import ro.mpp2025.interfaces.IRepository;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenericHibernateRepo <ID, T extends Entity<ID>> implements IRepository<ID, T> {

    protected final Class<T> entityType;

    @SuppressWarnings("unchecked")
    public GenericHibernateRepo() {
        this.entityType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[1];
    }

    public GenericHibernateRepo(Class<T> entityType) {
        this.entityType = entityType;
    }

    @Override
    public T findOne(ID id) {
        try(Session session = HibernateUtils.getSessionFactory().openSession()) {
            return session.get(entityType, id);
        }
    }

    @Override
    public Iterable<T> findAll() {
        try(Session session = HibernateUtils.getSessionFactory().openSession()) {
            String hql = "FROM "+entityType.getSimpleName();
            List<T> results = session.createQuery(hql, entityType).getResultList();
            return results != null ? results : new ArrayList<>();
        }
    }

    @Override
    public T save(T entity) {

        HibernateUtils.getSessionFactory().inTransaction(session -> {
            session.persist(entity);
        });
        return entity;
    }

    @Override
    public T delete(ID id) {
        T entity = findOne(id);
        if(entity != null) {
            HibernateUtils.getSessionFactory().inTransaction(session -> {
               T managedEntity = session.get(entityType, id);
               if(managedEntity != null) {
                   session.remove(managedEntity);
                   session.flush();
               }
            });
        }
        return entity;
    }

    @Override
    public T update(T entity) {
        if(entity != null || entity.getId() != null) {
            HibernateUtils.getSessionFactory().inTransaction(session -> {
               if(!Objects.isNull(session.find(entityType, entity.getId()))) {
                   session.merge(entity);
                   session.flush();
               }
            });
        }
        return entity;
    }
}
