package com.tfg.inventariado.providerImpl;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.tfg.inventariado.dto.MessageResponseDto;
import com.tfg.inventariado.dto.MessageResponseListDto;
import com.tfg.inventariado.dto.OficinaDto;
import com.tfg.inventariado.dto.OficinaFilterDto;
import com.tfg.inventariado.entity.OficinaEntity;
import com.tfg.inventariado.provider.OficinaProvider;
import com.tfg.inventariado.repository.OficinaRepository;

@Service
public class OficinaProviderImpl implements OficinaProvider {

	@Autowired
	private OficinaRepository oficinaRepository;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
    private MessageSource messageSource;
	
	@Override
	public OficinaDto convertToMapDto(OficinaEntity oficina) {
		return modelMapper.map(oficina, OficinaDto.class);
	}

	@Override
	public OficinaEntity convertToMapEntity(OficinaDto oficina) {
		return modelMapper.map(oficina, OficinaEntity.class);
	}

	@Override
	public List<OficinaDto> listAllOficinas() {
		List<OficinaEntity> listaOficinaEntity = oficinaRepository.findAll();
		return listaOficinaEntity.stream()
			        .sorted(Comparator.comparing(OficinaEntity::getDireccion, String.CASE_INSENSITIVE_ORDER))
			        .map(this::convertToMapDto)
			        .collect(Collectors.toList());
	}

	@Override
	public MessageResponseDto<Integer> addOficina(OficinaDto oficina) {
		Locale locale = LocaleContextHolder.getLocale();
		
		if(oficina.getDireccion()==null || oficina.getDireccion().isEmpty()) {
			return MessageResponseDto.fail(messageSource.getMessage("direccionObl", null, locale));
		}
		if(oficina.getLocalidad()==null || oficina.getLocalidad().isEmpty()) {
			return MessageResponseDto.fail(messageSource.getMessage("localidadObl", null, locale));
		}
		if(oficina.getPais()==null || oficina.getPais().isEmpty()) {
			return MessageResponseDto.fail(messageSource.getMessage("paisObl", null, locale));
		}
		OficinaEntity newOficina = convertToMapEntity(oficina);
		newOficina = oficinaRepository.save(newOficina);
		return MessageResponseDto.success(newOficina.getIdOficina());
	}

	@Override
	public MessageResponseDto<String> editOficina(OficinaDto oficina, Integer id) {
		Locale locale = LocaleContextHolder.getLocale();
		Optional<OficinaEntity> optionalOficina = oficinaRepository.findById(id);
		if(optionalOficina.isPresent()) {
			OficinaEntity oficinaToUpdate = optionalOficina.get();
			
			this.actualizarCampos(oficinaToUpdate, oficina);
			
			oficinaRepository.save(oficinaToUpdate);
			
			return MessageResponseDto.success(messageSource.getMessage("oficinaEditada", null, locale));
			
		}else {
			return MessageResponseDto.fail(messageSource.getMessage("oficinaNoExiste", null, locale));
		}
	}
	
	private void actualizarCampos(OficinaEntity oficina, OficinaDto oficinaToUpdate) {
		String cp = String.valueOf(oficinaToUpdate.getCodigoPostal());
		if(cp.length() == 5) {
			oficina.setCodigoPostal(oficinaToUpdate.getCodigoPostal());
		}
		if(StringUtils.isNotBlank(oficinaToUpdate.getDireccion())) {
			oficina.setDireccion(oficinaToUpdate.getDireccion());
		}
		if(StringUtils.isNotBlank(oficinaToUpdate.getLocalidad())) {
			oficina.setLocalidad(oficinaToUpdate.getLocalidad());
		}
		if(StringUtils.isNotBlank(oficinaToUpdate.getProvincia())) {
			oficina.setProvincia(oficinaToUpdate.getProvincia());
		}
		if(StringUtils.isNotBlank(oficinaToUpdate.getLocalidad())) {
			oficina.setLocalidad(oficinaToUpdate.getLocalidad());
		}
		if(StringUtils.isNotBlank(oficinaToUpdate.getPais())) {
			oficina.setPais(oficinaToUpdate.getPais());
		}
	}

	@Override
	public MessageResponseDto<OficinaDto> getOficinaById(Integer id) {
		Locale locale = LocaleContextHolder.getLocale();
		
		Optional<OficinaEntity> optionalOficina = oficinaRepository.findById(id);
		if(optionalOficina.isPresent()) {
			OficinaDto oficinaDto = this.convertToMapDto(optionalOficina.get());
			return MessageResponseDto.success(oficinaDto);
		}else {
			return MessageResponseDto.fail(messageSource.getMessage("oficinaNoExiste", null, locale));
		}
	}

	@Override
	public boolean oficinaExisteByID(Integer id) {
		Optional<OficinaEntity> optionalOficina = oficinaRepository.findById(id);
		return optionalOficina.isPresent() ? true : false;
	}

	@Override
	public MessageResponseListDto<List<OficinaDto>> listAllOficinasSkipLimit(Integer page, Integer size,
			OficinaFilterDto filtros) {
		Specification<OficinaEntity> spec = Specification.where(null);
		
		if (filtros != null) {
			if (filtros.getDireccion() != null) {
				String direc = filtros.getDireccion();
			    spec = spec.and((root, query, cb) -> cb.like(root.get("direccion"), "%" + direc + "%"));
	        }
			if (filtros.getCodigoPostal() != null) {
				Integer cp = filtros.getCodigoPostal();
			    spec = spec.and((root, query, cb) -> cb.equal(root.get("codigoPostal"), cp));
	        }
			if (filtros.getLocalidad() != null) {
				String loc = filtros.getLocalidad();
			    spec = spec.and((root, query, cb) -> cb.like(root.get("localidad"), "%" + loc + "%"));
	        }
			if (filtros.getProvincia() != null) {
				String prov = filtros.getProvincia();
			    spec = spec.and((root, query, cb) -> cb.like(root.get("provincia"), "%" + prov + "%"));
	        }
			if (filtros.getPais() != null) {
				String pais = filtros.getPais();
			    spec = spec.and((root, query, cb) -> cb.like(root.get("pais"), "%" + pais + "%"));
	        }
		}
		
		PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "localidad"));
		Page<OficinaEntity> pageableOficina = this.oficinaRepository.findAll(spec,pageable);
		
		List<OficinaEntity> listaEntity = pageableOficina.getContent();
		List<OficinaDto> listaDto = listaEntity.stream().map(this::convertToMapDto).collect(Collectors.toList());
		
		
		return MessageResponseListDto.success(listaDto, page, size,(int) this.oficinaRepository.count(spec));
	}

}
