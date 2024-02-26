package com.tfg.inventariado.providerImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tfg.inventariado.dto.HistorialInventarioDto;
import com.tfg.inventariado.dto.InventarioDto;
import com.tfg.inventariado.dto.MessageResponseDto;
import com.tfg.inventariado.dto.MessageResponseListDto;
import com.tfg.inventariado.entity.InventarioEntity;
import com.tfg.inventariado.entity.InventarioEntityID;
import com.tfg.inventariado.provider.ArticuloProvider;
import com.tfg.inventariado.provider.HistorialInventarioProvider;
import com.tfg.inventariado.provider.InventarioProvider;
import com.tfg.inventariado.provider.OficinaProvider;
import com.tfg.inventariado.repository.InventarioRepository;

@Service
public class InventarioProviderImpl implements InventarioProvider{

	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private InventarioRepository inventarioRepository;
	
	@Autowired
	private HistorialInventarioProvider historialProvider;
	
	@Autowired
	private OficinaProvider oficinaProvider;
	
	@Autowired
	private ArticuloProvider articuloProvider;
	
	@Override
	public InventarioDto convertToMapDto(InventarioEntity inventario) {
		return modelMapper.map(inventario, InventarioDto.class);
	}

	@Override
	public InventarioEntity convertToMapEntity(InventarioDto inventario) {
		return modelMapper.map(inventario, InventarioEntity.class);
	}

	@Override
	public List<InventarioDto> listAllInventarios() {

		List<InventarioEntity> listaEntity = inventarioRepository.findAll();
		return listaEntity.stream().map(this::convertToMapDto).collect(Collectors.toList());
	}

	@Transactional
	@Override
	public MessageResponseDto<String> addInventario(InventarioDto inventario) {
		if(inventario.getCodArticulo()==null || !this.articuloProvider.articuloExisteByID(inventario.getCodArticulo())) {
			return MessageResponseDto.fail("El artículo no existe");
		}
		if(inventario.getIdOficina()==null || !this.oficinaProvider.oficinaExisteByID(inventario.getIdOficina())) {
			return MessageResponseDto.fail("La oficina no existe");
		}
		InventarioEntityID id = new InventarioEntityID(inventario.getCodArticulo(), inventario.getIdOficina());
		
		if(inventarioRepository.findById(id).isPresent()) {
			return MessageResponseDto.fail("El inventario ya existe, debe editarlo");
		}
		
		HistorialInventarioDto historial = new HistorialInventarioDto(inventario.getCodArticulo(), inventario.getIdOficina(), LocalDateTime.now() , inventario.getStock());
		MessageResponseDto<String>  mesgHistorial = historialProvider.addHistorial(historial);
		if(!mesgHistorial.isSuccess()) {
			return MessageResponseDto.fail(mesgHistorial.getError());
		}
		InventarioEntity newInventario = convertToMapEntity(inventario);
		newInventario = inventarioRepository.save(newInventario);
		return MessageResponseDto.success("Inventario añadido con éxito");
	}

	@Transactional
	@Override
	public MessageResponseDto<String> editInventario(InventarioDto inventario, Integer idOf, Integer idArt) {
		InventarioEntityID id = new InventarioEntityID(idArt, idOf);
		Optional<InventarioEntity> optionalInventario = inventarioRepository.findById(id);
		if(optionalInventario.isPresent()) {
			HistorialInventarioDto historial = new HistorialInventarioDto(idArt, idOf, LocalDateTime.now() , inventario.getStock());
			MessageResponseDto<String>  mesgHistorial = historialProvider.addHistorial(historial);
			if(!mesgHistorial.isSuccess()) {
				return MessageResponseDto.fail(mesgHistorial.getError());
			}
			InventarioEntity inventarioToUpdate = optionalInventario.get();
			
			this.actualizarCampos(inventarioToUpdate, inventario);
			
			inventarioRepository.save(inventarioToUpdate);
			
			return MessageResponseDto.success("Inventario editado con éxito");
			
		}else {
			return MessageResponseDto.fail("El inventario que se desea editar no existe");
		}
	}
	
	private void actualizarCampos(InventarioEntity inventario, InventarioDto inventarioToUpdate) {
			
		inventario.setStock(inventarioToUpdate.getStock());
			
	}

	@Override
	public MessageResponseDto<InventarioDto> getInventarioById(Integer idOf, Integer idArt) {
		InventarioEntityID id = new InventarioEntityID(idArt, idOf);
		Optional<InventarioEntity> optional = inventarioRepository.findById(id);
		if(optional.isPresent()) {
			InventarioDto inventarioDto = this.convertToMapDto(optional.get());
			return MessageResponseDto.success(inventarioDto);
		}else {
			return MessageResponseDto.fail("No se encuentra ningún inventario con ese id");
		}
	}

	@Override
	public boolean inventarioExisteByID(Integer idOf, Integer idArt) {
		InventarioEntityID id = new InventarioEntityID(idArt, idOf);
		Optional<InventarioEntity> optional = inventarioRepository.findById(id);
		return optional.isPresent() ? true : false;
	}

	@Override
	public MessageResponseDto<List<InventarioDto>> listInventarioByOficina(Integer idOficina) {
		if(!this.oficinaProvider.oficinaExisteByID(idOficina)) {
			return MessageResponseDto.fail("La oficina no existe");
		}
		List<InventarioEntity> listaEntity = this.inventarioRepository.findByIdOficina(idOficina);
		List<InventarioDto> listaDto = listaEntity.stream().map(this::convertToMapDto).collect(Collectors.toList());
		return MessageResponseDto.success(listaDto);
	}

	@Override
	public MessageResponseDto<List<InventarioDto>> listInventarioByArticulo(Integer idArticulo) {
		if(!this.articuloProvider.articuloExisteByID(idArticulo)) {
			return MessageResponseDto.fail("El artículo no existe");
		}
		List<InventarioEntity> listaEntity = this.inventarioRepository.findByCodArticulo(idArticulo);
		List<InventarioDto> listaDto = listaEntity.stream().map(this::convertToMapDto).collect(Collectors.toList());
		return MessageResponseDto.success(listaDto);
	}

	@Override
	public MessageResponseListDto<List<InventarioDto>> listAllInventariosSkipLimit(Integer page, Integer size) {
		PageRequest pageable = PageRequest.of(page, size, Sort.by("idOficina", "codArticulo"));
		Page<InventarioEntity> pageableInventario = inventarioRepository.findAll(pageable);
		
		List<InventarioEntity> listaEntity = pageableInventario.getContent();
		List<InventarioDto> listaDto = listaEntity.stream().map(this::convertToMapDto).collect(Collectors.toList());
		
		return MessageResponseListDto.success(listaDto, page, size,(int) inventarioRepository.count());
	}

}
