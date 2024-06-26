package com.tfg.inventariado.provider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.tfg.inventariado.dto.HistorialInventarioDto;
import com.tfg.inventariado.dto.HistorialInventarioFilterDto;
import com.tfg.inventariado.dto.MessageResponseDto;
import com.tfg.inventariado.dto.MessageResponseListDto;
import com.tfg.inventariado.entity.HistorialInventarioEntity;

public interface HistorialInventarioProvider {

	HistorialInventarioDto convertToMapDto(HistorialInventarioEntity historial);
	HistorialInventarioEntity convertToMapEntity(HistorialInventarioDto historial);
	List<HistorialInventarioDto> listAllHistoriales();
	MessageResponseDto<String> addHistorial(HistorialInventarioDto historial);
	MessageResponseDto<String> editHistorial(HistorialInventarioDto historial, Integer idOf, Integer idArt, LocalDateTime fecha);
	MessageResponseDto<HistorialInventarioDto> getHistorialById(Integer idOf, Integer idArt, LocalDateTime fecha);
	
	MessageResponseDto<List<HistorialInventarioDto>> listHistorialByOficina(Integer idOficina);
	MessageResponseDto<List<HistorialInventarioDto>> listHistorialByArticulo(Integer idArticulo);
	
	boolean inventarioExisteByID(Integer idOf, Integer idArt, LocalDateTime fecha);
	
	MessageResponseListDto<List<HistorialInventarioDto>> listAllHistorialSkipLimit(Integer page, Integer size, HistorialInventarioFilterDto filtros);
	
	byte[] descargarExcelHistorialInventario(HistorialInventarioFilterDto filtros) throws IOException;
}
