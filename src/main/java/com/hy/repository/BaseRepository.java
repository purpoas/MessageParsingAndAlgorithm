package com.hy.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Created by Dylan on 2023/4/11 10:18.
 *
 * @author Dylan
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
    Slice<T> findAllSliced(@Nullable Specification<T> var1, Pageable var2);
}