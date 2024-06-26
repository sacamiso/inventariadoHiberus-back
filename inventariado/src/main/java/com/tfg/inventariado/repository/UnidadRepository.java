package com.tfg.inventariado.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tfg.inventariado.entity.UnidadEntity;

@Repository
public interface UnidadRepository  extends JpaRepository<UnidadEntity, Integer> {

	List<UnidadEntity> findByCodEstado(String cod);
	List<UnidadEntity> findByIdOficina(Integer idOficina);
	List<UnidadEntity> findByCodArticulo(Integer codArticulo);
	List<UnidadEntity> findByIdSalidaIsNullAndCodEstadoNot(String codEstado);
    List<UnidadEntity> findByIdSalidaIsNullAndCodEstadoNotAndIdOficina(String codEstado, Integer idOficina);
    List<UnidadEntity> findByIdSalidaIsNotNullOrCodEstado(String codEstado);
    List<UnidadEntity> findByIdSalidaIsNotNullAndIdOficina(Integer idOficina);
    
    Page<UnidadEntity> findAll(Specification<UnidadEntity> spec, Pageable pageable);
	long count(Specification<UnidadEntity> spec);
	
	@Query("SELECT COUNT(u) FROM UnidadEntity u WHERE u.idSalida = :idSalida")
    long countBySalidaId(@Param("idSalida") Integer idSalida);
	
	List<UnidadEntity> findByIdOficinaAndCodArticulo(Integer idOficina, Integer codArticulo);
	
	//Este método me da las unidades de una oficina concreta con un estado concreto, que no se les ha dado salida y que no están en ninguna asignación activa
    @Query("SELECT u FROM UnidadEntity u LEFT JOIN AsignacionEntity a ON u.codigoInterno = a.codUnidad AND a.fechaFin IS NULL WHERE u.idOficina = :idOficina AND u.codEstado = :codEstado AND a.idAsignacion IS NULL")
    List<UnidadEntity> findUnidadesLibresByEstadoAndOficina(@Param("codEstado") String codEstado, @Param("idOficina") Integer idOficina);
    
    @Query("SELECT u FROM UnidadEntity u LEFT JOIN AsignacionEntity a ON u.codigoInterno = a.codUnidad AND a.fechaFin IS NULL WHERE u.codEstado = 'OP' AND a.idAsignacion IS NULL")
    List<UnidadEntity> findUnidadesLibres();
    
    @Query("SELECT u FROM UnidadEntity u JOIN AsignacionEntity a ON u.codigoInterno = a.codUnidad AND a.fechaFin IS NULL")
    List<UnidadEntity> findUnidadesAsignadas();
    
}
