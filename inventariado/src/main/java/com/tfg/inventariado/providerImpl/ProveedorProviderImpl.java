package com.tfg.inventariado.providerImpl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tfg.inventariado.dto.MessageResponseDto;
import com.tfg.inventariado.dto.ProveedorDto;
import com.tfg.inventariado.entity.ProveedorEntity;
import com.tfg.inventariado.provider.ProveedorProvider;
import com.tfg.inventariado.repository.ProveedorRepository;

@Service
public class ProveedorProviderImpl implements ProveedorProvider {

	@Autowired
	private ProveedorRepository proveedorRepository;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Override
	public ProveedorDto convertToMapDto(ProveedorEntity proveedor) {
		return modelMapper.map(proveedor, ProveedorDto.class);
	}

	@Override
	public ProveedorEntity convertToMapEntity(ProveedorDto proveedor) {
		return modelMapper.map(proveedor, ProveedorEntity.class);
	}

	@Override
	public List<ProveedorDto> listAllProveedores() {
		List<ProveedorEntity> listaEntity = proveedorRepository.findAll();
		return listaEntity.stream().map(this::convertToMapDto).collect(Collectors.toList());
	}

	@Override
	public MessageResponseDto<String> addProveedor(ProveedorDto proveedor) {
		
		if(proveedorRepository.findById(proveedor.getIdProveedor()).isPresent()) {
			return MessageResponseDto.fail("El proveedor ya existe");
		}
		if(proveedor.getCif()==null || !StringUtils.isNotBlank(proveedor.getCif())) {
			return MessageResponseDto.fail("El CIF es obligatorio");
		}
		if(proveedorRepository.findByCif(proveedor.getCif()).isPresent()) {
			return MessageResponseDto.fail("Ya existe un proveedor con ese CIF");
		}
		if(proveedor.getRazonSocial()==null || !StringUtils.isNotBlank(proveedor.getRazonSocial())) {
			return MessageResponseDto.fail("La razon social es obligatoria");
		}
		if(proveedor.getDireccion()==null || !StringUtils.isNotBlank(proveedor.getDireccion())) {
			return MessageResponseDto.fail("La dirección es obligatoria");
		}
		String cp = String.valueOf(proveedor.getCodigoPostal());
		if(cp.length() != 5) {
			return MessageResponseDto.fail("El código postal es incorrecto");
		}
		if(proveedor.getLocalidad()==null || !StringUtils.isNotBlank(proveedor.getLocalidad())) {
			return MessageResponseDto.fail("La localidad es obligatoria");
		}
		if(proveedor.getTelefono()==null || !StringUtils.isNotBlank(proveedor.getTelefono())) {
			return MessageResponseDto.fail("El teléfono es obligatorio");
		}
		if(proveedor.getEmail()==null || !StringUtils.isNotBlank(proveedor.getEmail())) {
			return MessageResponseDto.fail("El email es obligatorio");
		}
		ProveedorEntity newProveedor = convertToMapEntity(proveedor);
		newProveedor = proveedorRepository.save(newProveedor);
		return MessageResponseDto.success("Proveedor añadido con éxito");
	}

	@Override
	public MessageResponseDto<String> editProveedor(ProveedorDto proveedor, Integer id) {
		Optional<ProveedorEntity> optionalProveedor = proveedorRepository.findById(id);
		if(optionalProveedor.isPresent()) {
			ProveedorEntity proveedorToUpdate = optionalProveedor.get();
			
			this.actualizarCampos(proveedorToUpdate, proveedor);
			
			proveedorRepository.save(proveedorToUpdate);
			
			return MessageResponseDto.success("Proveedor editado con éxito");
			
		}else {
			return MessageResponseDto.fail("El proveedor que se desea editar no existe");
		}
	}

	private void actualizarCampos(ProveedorEntity proveedor, ProveedorDto proveedorToUpdate) {
		
		if(StringUtils.isNotBlank(proveedorToUpdate.getCif()) && !proveedorRepository.findByCif(proveedorToUpdate.getCif()).isPresent()) {
			proveedor.setCif(proveedorToUpdate.getCif());
		}
		
		if(StringUtils.isNotBlank(proveedorToUpdate.getRazonSocial())) {
			proveedor.setRazonSocial(proveedorToUpdate.getRazonSocial());
		}
		if(StringUtils.isNotBlank(proveedorToUpdate.getDireccion())) {
			proveedor.setDireccion(proveedorToUpdate.getDireccion());
		}
		String cp = String.valueOf(proveedorToUpdate.getCodigoPostal());
		if(cp.length() == 5) {
			proveedor.setCodigoPostal(proveedorToUpdate.getCodigoPostal());
		}
		if(StringUtils.isNotBlank(proveedorToUpdate.getLocalidad())) {
			proveedor.setLocalidad(proveedorToUpdate.getLocalidad());
		}
		if(StringUtils.isNotBlank(proveedorToUpdate.getTelefono())) {
			proveedor.setTelefono(proveedorToUpdate.getTelefono());
		}
		if(StringUtils.isNotBlank(proveedorToUpdate.getEmail())) {
			proveedor.setEmail(proveedorToUpdate.getEmail());
		}
	}
	
	@Override
	public MessageResponseDto<ProveedorDto> getProveedorById(Integer id) {
		Optional<ProveedorEntity> optionalProveedor = proveedorRepository.findById(id);
		if(optionalProveedor.isPresent()) {
			ProveedorDto proveedorDto = this.convertToMapDto(optionalProveedor.get());
			return MessageResponseDto.success(proveedorDto);
		}else {
			return MessageResponseDto.fail("No se encuentra ningún proveedor con ese id");
		}
	}

	@Override
	public boolean proveedorExisteByID(Integer id) {
		Optional<ProveedorEntity> optionalProveedor = proveedorRepository.findById(id);
		return optionalProveedor.isPresent() ? true : false;	
	}

}
