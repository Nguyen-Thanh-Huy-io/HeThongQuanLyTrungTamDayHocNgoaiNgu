package vn.ute.service;

import java.util.List;
import java.util.Optional;

public interface Service<T, ID> {
    // Tên hàm phải khớp chính xác với AbstractService
    List<T> findAll() throws Exception; 
    
    Optional<T> findById(ID id) throws Exception;
}