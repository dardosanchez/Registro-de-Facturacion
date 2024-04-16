package com.java.Registro.Repository;

import com.java.Registro.Model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiciosRepository extends JpaRepository<Servicio,Integer> {

    @Query("SELECT s.nombre FROM Servicio s")
    List<String> findAllNombres();

}
