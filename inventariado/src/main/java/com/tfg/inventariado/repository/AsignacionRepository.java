package com.tfg.inventariado.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfg.inventariado.entity.AsignacionEntity;

@Repository
public interface AsignacionRepository extends JpaRepository<AsignacionEntity, Integer>{

	boolean existsByCodUnidadAndFechaFinIsNull(Integer codUnidad);
	List<AsignacionEntity> findByIdEmpleado(Integer idEmpleado);
	List<AsignacionEntity> findByFechaFinIsNullAndIdEmpleado(Integer idEmpleado);
	List<AsignacionEntity> findByFechaFinIsNotNullAndIdEmpleado(Integer idEmpleado);
	List<AsignacionEntity> findByCodUnidad(Integer codUnidad);
	List<AsignacionEntity> findByCodUnidadAndFechaFinIsNull(Integer codUnidad);
}
