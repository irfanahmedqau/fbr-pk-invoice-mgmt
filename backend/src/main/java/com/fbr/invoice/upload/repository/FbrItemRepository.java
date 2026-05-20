package com.fbr.invoice.upload.repository;




import org.springframework.data.jpa.repository.JpaRepository;

import com.fbr.invoice.upload.entity.FbrItem;

public interface FbrItemRepository extends JpaRepository<FbrItem, Long>{

}
