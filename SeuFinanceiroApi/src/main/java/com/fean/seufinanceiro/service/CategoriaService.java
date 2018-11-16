package com.fean.seufinanceiro.service;


import com.fean.seufinanceiro.model.Categoria;
import com.fean.seufinanceiro.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> showAll(){
        return (List<Categoria>) categoriaRepository.findAll();
    }

    public List<Categoria> showAllCategoryByUserId(Long id){
        return categoriaRepository.findAllByUsuarioId(id);
    }

    public Categoria showCategoriaById(Long id){
        return categoriaRepository.findById(id).get();
    }

    public Categoria showCategoriaByIdAndUserId(Long idCategoria, Long idUser){
        return categoriaRepository.findByIdAndUsuarioId(idCategoria, idCategoria);
    }


    public void novaCategoria(Categoria categoria){
        categoriaRepository.save(categoria);
    }

    public void removeCategoria(Long id){
        categoriaRepository.deleteById(id);
    }
}
