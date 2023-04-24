package com.hy.repository.impl;

import com.hy.repository.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Dylan on 2023/4/11 10:21.
 *
 * @author Dylan
 */
public class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {

    private final EntityManager entityManager;

    public BaseRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this.entityManager = entityManager;
    }

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public Slice<T> findAllSliced(Specification<T> spec, Pageable pageable) {
        TypedQuery<T> query = getQuery(spec, pageable.getSort());

        query.setFirstResult((int) pageable.getOffset());
        int extraSize = pageable.getPageSize() + 1;
        query.setMaxResults(extraSize);

        List<T> result = query.getResultList();
        boolean hasNext = result.size() == extraSize;

        if(hasNext){
            result.remove(extraSize - 1);
        }
        return new SliceImpl<>(result, pageable, hasNext);
    }

}
