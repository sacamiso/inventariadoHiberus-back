package com.tfg.inventariado.provider;

import java.util.List;

import com.tfg.inventariado.dto.EmpleadoDto;
import com.tfg.inventariado.dto.MessageResponseDto;
import com.tfg.inventariado.entity.EmpleadoEntity;

public interface EmpleadoProvider {
	
	EmpleadoDto convertToMapDto(EmpleadoEntity empleado);
	EmpleadoEntity convertToMapEntity(EmpleadoDto empleado);
	List<EmpleadoDto> listAllEmpleado();
	MessageResponseDto<String> addEmpleado(EmpleadoDto empleado);
	MessageResponseDto<String> editEmpleado(EmpleadoDto empleado, Integer id);
	MessageResponseDto<EmpleadoDto> getEmpleadoById(Integer id);
	MessageResponseDto<List<EmpleadoDto>> listEmpleadosByOficina(Integer idOficina);
	MessageResponseDto<List<EmpleadoDto>> listEmpleadosByRol(String codRol);
	
	boolean empleadoExisteByCodigo(Integer id);
	boolean empleadoExisteByUsuario(String usuario);
}
