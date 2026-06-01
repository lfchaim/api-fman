package com.corp.fmanager.repository;

import com.corp.fmanager.model.FileAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

/**
 * Repositório Spring Data JPA para a entidade FileAudit.
 */
@Repository
public interface FileAuditRepository extends JpaRepository<FileAudit, Long> {

    @Query("""
            SELECT a FROM FileAudit a
            WHERE (:operation IS NULL OR a.operation = :operation)
              AND (:startDate IS NULL OR a.operatedAt >= :startDate)
              AND (:endDate   IS NULL OR a.operatedAt <= :endDate)
            ORDER BY a.operatedAt DESC
            """)
    Page<FileAudit> findByFilters(
            @Param("operation")  FileAudit.Operation operation,
            @Param("startDate")  OffsetDateTime startDate,
            @Param("endDate")    OffsetDateTime endDate,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(a) FROM FileAudit a
            WHERE a.success = false
              AND a.operatedAt >= :since
            """)
    long countFailuresSince(@Param("since") OffsetDateTime since);
}
