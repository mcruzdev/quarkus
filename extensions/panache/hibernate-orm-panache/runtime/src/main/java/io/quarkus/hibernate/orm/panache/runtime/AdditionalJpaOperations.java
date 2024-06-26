package io.quarkus.hibernate.orm.panache.runtime;

import static io.quarkus.panache.hibernate.common.runtime.PanacheJpaUtil.createFindQuery;
import static io.quarkus.panache.hibernate.common.runtime.PanacheJpaUtil.toOrderBy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.Query;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

import org.hibernate.Session;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.CascadingActions;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metamodel.model.domain.internal.EntityTypeImpl;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.common.runtime.AbstractJpaOperations;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;

//TODO this class is only needed by the Spring Data JPA module and would not be placed there if it weren't for a dev-mode classloader issue
// see https://github.com/quarkusio/quarkus/issues/6214
public class AdditionalJpaOperations {

    @SuppressWarnings("rawtypes")
    public static PanacheQuery<?> find(AbstractJpaOperations<?> jpaOperations, Class<?> entityClass, String query,
            String countQuery, Sort sort, Map<String, Object> params) {
        String findQuery = createFindQuery(entityClass, query, jpaOperations.paramCount(params));
        Session session = jpaOperations.getSession(entityClass);
        Query jpaQuery = session.createQuery(sort != null ? findQuery + toOrderBy(sort) : findQuery);
        JpaOperations.bindParameters(jpaQuery, params);
        return new CustomCountPanacheQuery(session, jpaQuery, countQuery, params);
    }

    public static PanacheQuery<?> find(AbstractJpaOperations<?> jpaOperations, Class<?> entityClass, String query,
            String countQuery, Sort sort, Parameters parameters) {
        return find(jpaOperations, entityClass, query, countQuery, sort, parameters.map());
    }

    @SuppressWarnings("rawtypes")
    public static PanacheQuery<?> find(AbstractJpaOperations<?> jpaOperations, Class<?> entityClass, String query,
            String countQuery, Sort sort, Object... params) {
        String findQuery = createFindQuery(entityClass, query, jpaOperations.paramCount(params));
        Session session = jpaOperations.getSession(entityClass);
        Query jpaQuery = session.createQuery(sort != null ? findQuery + toOrderBy(sort) : findQuery);
        JpaOperations.bindParameters(jpaQuery, params);
        return new CustomCountPanacheQuery(session, jpaQuery, countQuery, params);
    }

    public static long deleteAllWithCascade(AbstractJpaOperations<?> jpaOperations, Class<?> entityClass) {
        Session session = jpaOperations.getSession(entityClass);
        //detecting the case where there are cascade-delete associations, and do the bulk delete query otherwise.
        if (deleteOnCascadeDetected(jpaOperations, entityClass)) {
            int count = 0;
            List<?> objects = jpaOperations.listAll(entityClass);
            for (Object entity : objects) {
                session.remove(entity);
                count++;
            }
            return count;
        }
        return jpaOperations.deleteAll(entityClass);
    }

    /**
     * Detects if cascading delete is needed. The delete-cascading is needed when associations with cascade delete enabled
     * {@link jakarta.persistence.OneToMany#cascade()} and also on entities containing a collection of elements
     * {@link jakarta.persistence.ElementCollection}
     *
     * @param entityClass
     * @return true if cascading delete is needed. False otherwise
     */
    private static boolean deleteOnCascadeDetected(AbstractJpaOperations<?> jpaOperations, Class<?> entityClass) {
        Session session = jpaOperations.getSession(entityClass);
        Metamodel metamodel = session.getMetamodel();
        EntityType<?> entity1 = metamodel.entity(entityClass);
        Set<Attribute<?, ?>> declaredAttributes = ((EntityTypeImpl) entity1).getDeclaredAttributes();

        CascadeStyle[] propertyCascadeStyles = session.unwrap(SessionImplementor.class)
                .getEntityPersister(entityClass.getName(), null)
                .getPropertyCascadeStyles();
        boolean doCascade = Arrays.stream(propertyCascadeStyles)
                .anyMatch(cascadeStyle -> cascadeStyle.doCascade(CascadingActions.DELETE));
        boolean hasElementCollection = declaredAttributes.stream()
                .anyMatch(attribute -> attribute.getPersistentAttributeType()
                        .equals(Attribute.PersistentAttributeType.ELEMENT_COLLECTION));
        return doCascade || hasElementCollection;

    }

    public static <PanacheQueryType> long deleteWithCascade(AbstractJpaOperations<PanacheQueryType> jpaOperations,
            Class<?> entityClass, String query, Object... params) {
        Session session = jpaOperations.getSession(entityClass);
        if (deleteOnCascadeDetected(jpaOperations, entityClass)) {
            int count = 0;
            List<?> objects = jpaOperations.list(jpaOperations.find(entityClass, query, params));
            for (Object entity : objects) {
                session.remove(entity);
                count++;
            }
            return count;
        }
        return jpaOperations.delete(entityClass, query, params);
    }

    public static <PanacheQueryType> long deleteWithCascade(AbstractJpaOperations<PanacheQueryType> jpaOperations,
            Class<?> entityClass, String query,
            Map<String, Object> params) {
        Session session = jpaOperations.getSession(entityClass);
        if (deleteOnCascadeDetected(jpaOperations, entityClass)) {
            int count = 0;
            List<?> objects = jpaOperations.list(jpaOperations.find(entityClass, query, params));
            for (Object entity : objects) {
                session.remove(entity);
                count++;
            }
            return count;
        }
        return jpaOperations.delete(entityClass, query, params);
    }

    public static long deleteWithCascade(AbstractJpaOperations<?> jpaOperations, Class<?> entityClass, String query,
            Parameters params) {
        return deleteWithCascade(jpaOperations, entityClass, query, params.map());
    }
}
