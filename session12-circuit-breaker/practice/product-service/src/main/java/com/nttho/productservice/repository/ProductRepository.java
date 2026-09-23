package com.nttho.productservice.repository;

import com.nttho.productservice.entity.Product;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;


public class ProductRepository {
    public static final List<Product> listProduct = new ArrayList<>();
}
