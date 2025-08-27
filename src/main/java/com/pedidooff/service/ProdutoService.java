package com.pedidooff.service;

import com.pedidooff.model.Produto;
import com.pedidooff.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public Produto salvar(Produto p) { return repository.save(p); }

    public List<Produto> listar() { return repository.findAll(); }
}
