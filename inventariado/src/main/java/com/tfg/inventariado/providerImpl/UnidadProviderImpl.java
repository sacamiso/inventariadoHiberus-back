package com.tfg.inventariado.providerImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.tfg.inventariado.dto.ArticuloDto;
import com.tfg.inventariado.dto.LineaDto;
import com.tfg.inventariado.dto.MessageResponseDto;
import com.tfg.inventariado.dto.MessageResponseListDto;
import com.tfg.inventariado.dto.PedidoDto;
import com.tfg.inventariado.dto.SalidaDto;
import com.tfg.inventariado.dto.UnidadDto;
import com.tfg.inventariado.dto.UnidadFilterDto;
import com.tfg.inventariado.entity.ArticuloEntity;
import com.tfg.inventariado.entity.AsignacionEntity;
import com.tfg.inventariado.entity.InventarioEntity;
import com.tfg.inventariado.entity.UnidadEntity;
import com.tfg.inventariado.provider.ArticuloProvider;
import com.tfg.inventariado.provider.EstadoProvider;
import com.tfg.inventariado.provider.LineaProvider;
import com.tfg.inventariado.provider.OficinaProvider;
import com.tfg.inventariado.provider.PedidoProvider;
import com.tfg.inventariado.provider.SalidaProvider;
import com.tfg.inventariado.provider.UnidadProvider;
import com.tfg.inventariado.repository.AsignacionRepository;
import com.tfg.inventariado.repository.InventarioRepository;
import com.tfg.inventariado.repository.UnidadRepository;

@Service
public class UnidadProviderImpl implements UnidadProvider {

	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private UnidadRepository unidadRepository;
		
	@Autowired
	private OficinaProvider oficinaProvider;
	
	@Autowired
	private ArticuloProvider articuloProvider;
	
	@Autowired
	private EstadoProvider estadoProvider;
	
	@Autowired
	private PedidoProvider pedidoProvider;
	
	@Autowired
	private SalidaProvider salidaProvider;
		
	@Autowired
	private AsignacionRepository asignacionRepository;
	
	@Autowired
	private InventarioRepository inventarioRepository;
	
	@Autowired
	private LineaProvider lineaProvider;
	
	@Autowired
    private MessageSource messageSource;
	
	@Override
	public UnidadDto convertToMapDto(UnidadEntity unidad) {
		return modelMapper.map(unidad, UnidadDto.class);
	}

	@Override
	public UnidadEntity convertToMapEntity(UnidadDto unidad) {
		return modelMapper.map(unidad, UnidadEntity.class);
	}

	@Override
	public List<UnidadDto> listAllUnidades() {
		List<UnidadEntity> listaEntity = unidadRepository.findAll();
		return listaEntity.stream()
				.sorted(Comparator.comparing(UnidadEntity::getCodigoInterno))
				.map(this::convertToMapDto).collect(Collectors.toList());
	}

	@Override
	public MessageResponseDto<Integer> addUnidad(UnidadDto unidad) {
		Locale locale = LocaleContextHolder.getLocale();
		
		if(unidad.getCodigoInterno()==null) {
			return MessageResponseDto.fail(messageSource.getMessage("ciObligatorio", null, locale));
		}
		if(unidadRepository.existsById(unidad.getCodigoInterno())){
			return MessageResponseDto.fail(messageSource.getMessage("ciExiste", null, locale));
		}
		if(unidad.getCodEstado()==null || unidad.getCodEstado().isEmpty()) {
			return MessageResponseDto.fail(messageSource.getMessage("estadoObl", null, locale));
		}
		if(!this.estadoProvider.estadoExisteByCodigo(unidad.getCodEstado())) {
			return MessageResponseDto.fail(messageSource.getMessage("estadoNoExiste", null, locale));
		}
		if(unidad.getNumeroPedido()!= null && !this.pedidoProvider.pedidoExisteByID(unidad.getNumeroPedido())) {
			return MessageResponseDto.fail(messageSource.getMessage("pedidoNoExiste", null, locale));
		}
		if(unidad.getCodArticulo()!= null && !this.articuloProvider.articuloExisteByID(unidad.getCodArticulo())) {
			return MessageResponseDto.fail(messageSource.getMessage("articuloNoExiste", null, locale));
		}
		if(unidad.getIdOficina()!= null && !this.oficinaProvider.oficinaExisteByID(unidad.getIdOficina())) {
			return MessageResponseDto.fail(messageSource.getMessage("oficinaNoExiste", null, locale));
		}
		if(unidad.getIdSalida()!= null && !this.salidaProvider.salidaExisteByID(unidad.getIdSalida())) {
			return MessageResponseDto.fail(messageSource.getMessage("salidaNoExiste", null, locale));
		}else {
			if(unidad.getIdSalida()!= null) {
				MessageResponseDto<SalidaDto> salida = this.salidaProvider.getSalidaById(unidad.getIdSalida());
				if(salida.getMessage().getCodArticulo() != unidad.getCodArticulo()) {
					return MessageResponseDto.fail(messageSource.getMessage("salidaNoArt", null, locale));
				}
				if(salida.getMessage().getIdOficina() != unidad.getIdOficina()) {
					return MessageResponseDto.fail(messageSource.getMessage("salidaNoOficina", null, locale));
				}
			}
			
			//Se podría comprobar si se ha dado salida ya a tantas unidades como pone en la salida y por ello no se puede dar salida a esta
			
		}
		
		UnidadEntity newUnidad = convertToMapEntity(unidad);
		newUnidad = unidadRepository.save(newUnidad);
		return MessageResponseDto.success(newUnidad.getCodigoInterno());
	}

	@Override
	public MessageResponseDto<String> editUnidad(UnidadDto unidad, Integer id) {
		Locale locale = LocaleContextHolder.getLocale();
		try {
			Optional<UnidadEntity> optionalUnidad = unidadRepository.findById(id);
			if(optionalUnidad.isPresent()) {
				
				UnidadEntity unidadToUpdate = optionalUnidad.get();
				
				MessageResponseDto<String> msg = this.actualizarCampos(unidadToUpdate, unidad);
				if(!msg.isSuccess()) {
					return msg;
				}
				
				unidadRepository.save(unidadToUpdate);
				
				return msg;
				
			}else {
				return MessageResponseDto.fail(messageSource.getMessage("unidadNoExiste", null, locale));
			}
		} catch (Exception e) {
			return MessageResponseDto.fail("Error: " + e.getMessage());
		}
	}
	
	private MessageResponseDto<String> actualizarCampos(UnidadEntity unidad, UnidadDto unidadToUpdate) {
		Locale locale = LocaleContextHolder.getLocale();

		if(unidadToUpdate.getCodEstado()!=null && !unidadToUpdate.getCodEstado().isEmpty() && this.estadoProvider.estadoExisteByCodigo(unidadToUpdate.getCodEstado())) {
			if(unidadToUpdate.getCodEstado().equals("OP")&&unidad.getCodEstado().equals("MANT")) {
				unidad.setCodEstado(unidadToUpdate.getCodEstado());
			}
			else if(unidadToUpdate.getCodEstado().equals("MANT")&&unidad.getCodEstado().equals("OP")) {
				List<AsignacionEntity> listaEntity = this.asignacionRepository.findByCodUnidadAndFechaFinIsNull(unidad.getCodigoInterno());
				if(listaEntity.size()>0) {
					return MessageResponseDto.fail(messageSource.getMessage("noMant", null, locale));
				}
				unidad.setCodEstado(unidadToUpdate.getCodEstado());
			}
			else if(unidadToUpdate.getCodEstado().equals("S")&&unidad.getCodEstado().equals("OP") || unidadToUpdate.getCodEstado().equals("S")&&unidad.getCodEstado().equals("MANT")) {
				List<AsignacionEntity> listaEntity = this.asignacionRepository.findByCodUnidadAndFechaFinIsNull(unidad.getCodigoInterno());
				if(listaEntity.size()>0) {
					return MessageResponseDto.fail(messageSource.getMessage("noS", null, locale));
				}
				unidad.setCodEstado(unidadToUpdate.getCodEstado());
			}
			else if(unidadToUpdate.getCodEstado().equals("OP")&& unidad.getCodEstado().equals("S") || unidadToUpdate.getCodEstado().equals("MANT")&&unidad.getCodEstado().equals("S")) {
				unidad.setCodEstado(unidadToUpdate.getCodEstado());
				unidad.setIdSalida(null);
			}
			
		}
		if(unidadToUpdate.getNumeroPedido()!= null && this.pedidoProvider.pedidoExisteByID(unidadToUpdate.getNumeroPedido())) {
			unidad.setNumeroPedido(unidadToUpdate.getNumeroPedido());
		}
		if(unidadToUpdate.getCodArticulo()!= null && this.articuloProvider.articuloExisteByID(unidadToUpdate.getCodArticulo())) {
			unidad.setCodArticulo(unidadToUpdate.getCodArticulo());
		}
		if(unidadToUpdate.getIdOficina()!= null && this.oficinaProvider.oficinaExisteByID(unidadToUpdate.getIdOficina())) {
			unidad.setIdOficina(unidadToUpdate.getIdOficina());
		}
		if(unidadToUpdate.getIdSalida()!= null && this.salidaProvider.salidaExisteByID(unidadToUpdate.getIdSalida())) {
			MessageResponseDto<SalidaDto> salida = this.salidaProvider.getSalidaById(unidadToUpdate.getIdSalida());
			if( salida.isSuccess() && salida.getMessage().getIdOficina() != unidad.getIdOficina()) {
				return MessageResponseDto.fail(messageSource.getMessage("salidaNoOfi", null, locale));
			}
			if( salida.isSuccess() && salida.getMessage().getCodArticulo() != unidad.getCodArticulo()) {
				return MessageResponseDto.fail(messageSource.getMessage("salidaNoArt", null, locale));

			}
			if( salida.isSuccess() && salida.getMessage().getNumUnidades() <= unidadRepository.countBySalidaId(unidadToUpdate.getIdSalida())) {
				return MessageResponseDto.fail(messageSource.getMessage("salidaNoMasUni", null, locale));
			}
			unidad.setIdSalida(unidadToUpdate.getIdSalida());
		}	
		return MessageResponseDto.success(messageSource.getMessage("unidadEditada", null, locale)); 	
	}

	@Override
	public MessageResponseDto<UnidadDto> getUnidadById(Integer id) {
		Locale locale = LocaleContextHolder.getLocale();
		Optional<UnidadEntity> optional = unidadRepository.findById(id);
		if(optional.isPresent()) {
			UnidadDto unidadDto = this.convertToMapDto(optional.get());
			return MessageResponseDto.success(unidadDto);
		}else {
			return MessageResponseDto.fail(messageSource.getMessage("unidadNoExiste", null, locale));
		}
	}

	@Override
	public MessageResponseDto<List<UnidadDto>> listUnidadByEstado(String codEstado) {
		Locale locale = LocaleContextHolder.getLocale();
		if(!this.estadoProvider.estadoExisteByCodigo(codEstado)) {
			return MessageResponseDto.fail(messageSource.getMessage("estadoNoExiste", null, locale));
		}
		List<UnidadEntity> listaEntity = this.unidadRepository.findByCodEstado(codEstado);
		List<UnidadDto> listaDto = listaEntity.stream()
				.sorted(Comparator.comparing(UnidadEntity::getCodigoInterno))
				.map(this::convertToMapDto).collect(Collectors.toList());
		return MessageResponseDto.success(listaDto);
	}

	@Override
	public MessageResponseDto<List<UnidadDto>> listUnidadDisponibles() {
		List<UnidadEntity> listaEntity = this.unidadRepository.findByIdSalidaIsNullAndCodEstadoNot("MANT");
		List<UnidadDto> listaDto = listaEntity.stream()
				.sorted(Comparator.comparing(UnidadEntity::getCodigoInterno))
				.map(this::convertToMapDto).collect(Collectors.toList());
		return MessageResponseDto.success(listaDto);
	}

	@Override
	public MessageResponseDto<List<UnidadDto>> listUnidadDisponiblesByOficina(Integer idOficina) {
		Locale locale = LocaleContextHolder.getLocale();
		if(!this.oficinaProvider.oficinaExisteByID(idOficina)) {
			return MessageResponseDto.fail(messageSource.getMessage("oficinaNoExiste", null, locale));
		}
		List<UnidadEntity> listaEntity = this.unidadRepository.findByIdSalidaIsNullAndCodEstadoNotAndIdOficina("MANT",idOficina);
		List<UnidadDto> listaDto = listaEntity.stream()
				.sorted(Comparator.comparing(UnidadEntity::getCodigoInterno))
				.map(this::convertToMapDto).collect(Collectors.toList());
		return MessageResponseDto.success(listaDto);
	}

	@Override
	public MessageResponseDto<List<UnidadDto>> listUnidadNODisponibles() {
		List<UnidadEntity> listaEntity = this.unidadRepository.findByIdSalidaIsNotNullOrCodEstado("S");
		List<UnidadDto> listaDto = listaEntity.stream()
				.sorted(Comparator.comparing(UnidadEntity::getCodigoInterno))
				.map(this::convertToMapDto).collect(Collectors.toList());
		return MessageResponseDto.success(listaDto);
	}

	@Override
	public MessageResponseDto<List<UnidadDto>> listUnidadNODisponiblesByOficina(Integer idOficina) {
		Locale locale = LocaleContextHolder.getLocale();
		if(!this.oficinaProvider.oficinaExisteByID(idOficina)) {
			return MessageResponseDto.fail(messageSource.getMessage("oficinaNoExiste", null, locale));
		}
		List<UnidadEntity> listaEntity = this.unidadRepository.findByIdSalidaIsNotNullAndIdOficina(idOficina);
		List<UnidadDto> listaDto = listaEntity.stream()
				.sorted(Comparator.comparing(UnidadEntity::getCodigoInterno))
				.map(this::convertToMapDto).collect(Collectors.toList());
		return MessageResponseDto.success(listaDto);
	}

	@Override
	public MessageResponseDto<List<UnidadDto>> listUnidadesByOficina(Integer idOficina) {
		Locale locale = LocaleContextHolder.getLocale();
		if(!this.oficinaProvider.oficinaExisteByID(idOficina)) {
			return MessageResponseDto.fail(messageSource.getMessage("oficinaNoExiste", null, locale));
		}
		List<UnidadEntity> listaEntity = this.unidadRepository.findByIdOficina(idOficina);
		List<UnidadDto> listaDto = listaEntity.stream()
				.sorted(Comparator.comparing(UnidadEntity::getCodigoInterno))
				.map(this::convertToMapDto).collect(Collectors.toList());
		return MessageResponseDto.success(listaDto);
	}

	@Override
	public MessageResponseDto<List<UnidadDto>> listUnidadByArticulo(Integer idArticulo) {
		Locale locale = LocaleContextHolder.getLocale();
		if(!this.articuloProvider.articuloExisteByID(idArticulo)) {
			return MessageResponseDto.fail(messageSource.getMessage("articuloNoExiste", null, locale));
		}
		List<UnidadEntity> listaEntity = this.unidadRepository.findByCodArticulo(idArticulo);
		List<UnidadDto> listaDto = listaEntity.stream()
				.sorted(Comparator.comparing(UnidadEntity::getCodigoInterno))
				.map(this::convertToMapDto).collect(Collectors.toList());
		return MessageResponseDto.success(listaDto);
	}

	@Override
	public boolean unidadExisteByID(Integer id) {
		Optional<UnidadEntity> optional = unidadRepository.findById(id);
		return optional.isPresent() ? true : false;
	}

	@Override
	public MessageResponseDto<String> darSalidaUnidad(Integer idSalida, Integer idUnidad) {
		Locale locale = LocaleContextHolder.getLocale();

		Optional<UnidadEntity> optionalUnidad = unidadRepository.findById(idUnidad);
		MessageResponseDto<SalidaDto> salida = salidaProvider.getSalidaById(idSalida);
		if(!salida.isSuccess()) {
			return MessageResponseDto.fail(messageSource.getMessage("salidaNoExiste", null, locale));
		}
		
		if(optionalUnidad.isPresent()) {
			
			UnidadEntity unidad = optionalUnidad.get();
			if(salida.getMessage().getCodArticulo() != unidad.getCodArticulo() && salida.getMessage().getIdOficina() != unidad.getIdOficina()) {
				return MessageResponseDto.fail("la salida elegida no vale para esta unidad");
			}
			if(asignacionRepository.existsByCodUnidadAndFechaFinIsNull(idUnidad)) {
				return MessageResponseDto.fail(messageSource.getMessage("salidaInvForUnidad", null, locale));
			}

			unidad.setIdSalida(idSalida);
			unidadRepository.save(unidad);
			
			return MessageResponseDto.success(messageSource.getMessage("unidadEditada", null, locale));
			
		}else {
			return MessageResponseDto.fail(messageSource.getMessage("unidadNoExiste", null, locale));
		}
	}

	@Override
	public MessageResponseListDto<List<UnidadDto>> listAllUnidadesSkipLimit(Integer page, Integer size, UnidadFilterDto filtros) {

		Specification<UnidadEntity> spec = Specification.where(null);
		if (filtros != null) {
			if (filtros.getCodEstado()!=null && !filtros.getCodEstado().isEmpty()) {
	            String codEstado = filtros.getCodEstado();
	            spec = spec.and((root, query, cb) -> cb.equal(root.get("codEstado"), codEstado));
	        }
			if (filtros.getIdOficina()!= null && filtros.getIdOficina()!= 0) {
	            Integer idOficina = filtros.getIdOficina();
	            spec = spec.and((root, query, cb) -> cb.equal(root.get("idOficina"), idOficina));
	        }
			if (filtros.getFechaPedido() != null) {
				LocalDate fechaPedido = filtros.getFechaPedido();
	            spec = spec.and((root, query, cb) -> cb.equal(root.join("pedido").get("fechaPedido"), fechaPedido));
	        }
			if (filtros.getFechaSalida() != null) {
				LocalDate fechaSalida = filtros.getFechaSalida();
	            spec = spec.and((root, query, cb) -> cb.equal(root.join("salida").get("fechaSalida"), fechaSalida));
	        }
			if (filtros.getCodArticulo()!= null && filtros.getCodArticulo()!= 0) {
	            Integer codArticulo = filtros.getCodArticulo();
	            spec = spec.and((root, query, cb) -> cb.equal(root.get("codArticulo"), codArticulo));
	        }
			if (filtros.getDisponible() != null) {
				List<UnidadEntity> unidadesDisponibles = this.unidadRepository.findUnidadesLibres();
			    if (filtros.getDisponible()) {
			        spec = spec.and((root, query, cb) -> cb.isTrue(root.in(unidadesDisponibles)));
			    } else {
			        spec = spec.and((root, query, cb) -> cb.not(root.in(unidadesDisponibles)));
			    }
			}

		}
		
		PageRequest pageable = PageRequest.of(page, size, Sort.by("idOficina", "codArticulo", "codigoInterno"));
		Page<UnidadEntity> pageableUnidad = unidadRepository.findAll(spec, pageable);
		
		List<UnidadEntity> listaEntity = pageableUnidad.getContent();
		List<UnidadDto> listaDto = listaEntity.stream().map(this::convertToMapDto).collect(Collectors.toList());
		
		return MessageResponseListDto.success(listaDto, page, size,(int) unidadRepository.count(spec));
	}

	@Override
	public List<ArticuloDto> listaArticulosDisponiblesEnInventarioParaRegistrarUnidadesByOficina(Integer idOficina) {
		// Contar unidades por cada artículo para la oficina dada
		List<UnidadEntity> unidades = unidadRepository.findByIdOficina(idOficina);
		Map<Integer, Long> unidadesPorArticulo = unidades.stream().collect(Collectors.groupingBy(UnidadEntity::getCodArticulo, Collectors.counting()));

        // Obtener las entidades de inventario para la misma oficina
		List<InventarioEntity> inventario = inventarioRepository.findByIdOficinaOrderByArticuloReferenciaAsc(idOficina);

		List<ArticuloEntity> listArticulos = new ArrayList<ArticuloEntity>();
		
		for (InventarioEntity inventarioEntity : inventario) {
            Integer codArticulo = inventarioEntity.getCodArticulo();
            if (unidadesPorArticulo.containsKey(codArticulo)) {
                Long cantidadUnidades = unidadesPorArticulo.get(codArticulo);
                if (inventarioEntity.getStock() > cantidadUnidades.intValue()) {
                	listArticulos.add(inventarioEntity.getArticulo());
                }
            } else {
                listArticulos.add(inventarioEntity.getArticulo());
            }
        }
		
		List<ArticuloDto> listaDto = listArticulos.stream().map(this.articuloProvider::convertToMapDto).collect(Collectors.toList());

		return listaDto;
	}
	
	@Override
	public MessageResponseDto<List<PedidoDto>> pedidosDisponiblesByOficinaAndArticulo(Integer idOficina, Integer codArticulo){
		// Contar el número de unidades por cada numeroPedido que cumplan con los criterios
        List<UnidadEntity> unidades = unidadRepository.findByIdOficinaAndCodArticulo(idOficina, codArticulo);
        unidades.removeIf(unidad -> unidad.getNumeroPedido() == null);
        Map<Integer, Long> unidadesPorPedido = unidades.stream().collect(Collectors.groupingBy(UnidadEntity::getNumeroPedido, Collectors.counting()));

        MessageResponseDto<List<PedidoDto>> msgPedidosDto = this.pedidoProvider.listPedidoByOficina(idOficina);
        if(!msgPedidosDto.isSuccess()) {
			return msgPedidosDto;
        }
        
        List<PedidoDto> pedidosDto = msgPedidosDto.getMessage();
        pedidosDto.removeIf(pedido -> pedido.getFechaRecepcion() == null);
        
        List<LineaDto> lineasDto = new ArrayList<LineaDto>();
        
        for (PedidoDto pedido : pedidosDto) {
        	lineasDto.addAll( this.lineaProvider.listLineasByPedido(pedido.getNumeroPedido()).getMessage());
        }
        
        lineasDto.removeIf(linea -> !linea.getCodigoArticulo().equals(codArticulo));
        
        Map<Integer, Integer> mapaSumaUnidadesPorPedido = lineasDto.stream().collect(Collectors.groupingBy(LineaDto::getNumeroPedido,Collectors.summingInt(LineaDto::getNumeroUnidades)));
      
        List<PedidoDto> pedidosMostrar = new ArrayList<PedidoDto>();
        
        for(Integer codPed : mapaSumaUnidadesPorPedido.keySet()) {
        	Integer valor = mapaSumaUnidadesPorPedido.get(codPed);
        	
        	if (unidadesPorPedido.containsKey(codPed)) {
                Long cantidadUnidades = unidadesPorPedido.get(codPed);
                if (valor > cantidadUnidades.intValue()) {
                	pedidosMostrar.add(this.pedidoProvider.getPedidoById(codPed).getMessage());
                }
            } else {
            	pedidosMostrar.add(this.pedidoProvider.getPedidoById(codPed).getMessage());
            }
        }
        
		return MessageResponseDto.success(pedidosMostrar);
 
	}

	@Override
	public MessageResponseDto<List<UnidadDto>> listUnidadDisponiblesSinAsignarByOficina(Integer idOficina) {
		Locale locale = LocaleContextHolder.getLocale();
		if(!this.oficinaProvider.oficinaExisteByID(idOficina)) {
			return MessageResponseDto.fail(messageSource.getMessage("oficinaNoExiste", null, locale));
		}
		List<UnidadEntity> listaEntity = this.unidadRepository.findUnidadesLibresByEstadoAndOficina("OP",idOficina);
		List<UnidadDto> listaDto = listaEntity.stream()
				.sorted(Comparator.comparing(UnidadEntity::getCodigoInterno))
				.map(this::convertToMapDto).collect(Collectors.toList());
		return MessageResponseDto.success(listaDto);
	}

	@Override
	public MessageResponseDto<Boolean> estaAsignada(Integer codInterno) {
		List<UnidadEntity> unidadesAsignadas = this.unidadRepository.findUnidadesAsignadas();
		for (UnidadEntity unidadEntity : unidadesAsignadas) {
			if(codInterno.equals(unidadEntity.getCodigoInterno())) {
				return MessageResponseDto.success(true);
			}
		}
		return MessageResponseDto.success(false);
	}

	private XSSFCellStyle headerStyle(XSSFWorkbook workbook) {
		XSSFCellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		XSSFFont headerFont = workbook.createFont();
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		headerFont.setBold(true);
		headerStyle.setFont(headerFont);
		return headerStyle;
	}
	
	private XSSFCellStyle headerStyleLinea(XSSFWorkbook workbook) {
		XSSFCellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		XSSFFont headerFont = workbook.createFont();
		headerFont.setColor(IndexedColors.BLACK.getIndex());
		headerFont.setBold(true);
		headerStyle.setFont(headerFont);
		return headerStyle;
	}

	@Override
	public byte[] descargarExcelUnidadById(Integer id) throws IOException {

		MessageResponseDto<UnidadDto> unidadMSG = this.getUnidadById(id);
		if(!unidadMSG.isSuccess()) {
			throw new IOException("No se ha encontrado la unidad");
		}
		
		UnidadDto unidad = unidadMSG.getMessage();
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet hoja = workbook.createSheet("Unidad " + id);
		
		XSSFCellStyle headerStyle = headerStyle(workbook);
		
		String[] encabezados = { "código interno", "Estado", "Asignado", "Detalles pedido", "Detalles salida"};
		
		int indiceFila = 1;
		XSSFRow fila = hoja.createRow(indiceFila);
		
		for (int i = 0; i < encabezados.length; i++) {
			String encabezado = encabezados[i];
			XSSFCell celda = fila.createCell(i);
			celda.setCellValue(encabezado);
			celda.setCellStyle(headerStyle);
		}
		
		HashMap<String, XSSFCellStyle> styles = new HashMap<>();
		styles.put("HEADER", headerStyle);
		
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		
		// Formato de fecha
		CreationHelper creationHelper = workbook.getCreationHelper();
		CellStyle dateCellStyle = workbook.createCellStyle();
		dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd/mm/yyyy"));
		
		indiceFila++;
		
		fila = hoja.createRow(indiceFila);
		
		fila.createCell(0).setCellValue(unidad.getCodigoInterno());
		fila.createCell(1).setCellValue(unidad.getEstado().getCodigoEstado() + ": " + unidad.getEstado().getNombre());
		
		boolean asignado = this.estaAsignada(id).getMessage();
		if(asignado) {
			fila.createCell(2).setCellValue("SI");
		}else {
			fila.createCell(2).setCellValue("NO");
		}
		
		if(unidad.getPedido()!=null) {
			fila.createCell(3).setCellValue("SI");
		}else {
			fila.createCell(3).setCellValue("NO");
		}
		
		if(unidad.getSalida()!=null) {
			fila.createCell(4).setCellValue("SI");
		}else {
			fila.createCell(4).setCellValue("NO");
		}
		
		indiceFila++;
		indiceFila++;
		
		String[] encabezadosArtículo = { "Referencia", "Descripción", "Precio (€)", "IVA (%)", "Categoría",
				"Subcategoría", "Fabricante", "Modelo" };
		
		fila = hoja.createRow(indiceFila);
		XSSFCell celda = fila.createCell(0);
		celda.setCellValue("ARTÍCULO");
		celda.setCellStyle(headerStyle);
		indiceFila++;
		indiceFila++;
		
		XSSFCellStyle headerStyleLinea = headerStyleLinea(workbook);
		fila = hoja.createRow(indiceFila);
		for (int i = 0; i < encabezadosArtículo.length; i++) {
			String encabezadoL = encabezadosArtículo[i];
			XSSFCell celda1 = fila.createCell(i);
			celda1.setCellValue(encabezadoL);
			celda1.setCellStyle(headerStyleLinea);
		}
		
		indiceFila++;
		fila = hoja.createRow(indiceFila);
		fila.createCell(0).setCellValue(unidad.getArticulo().getReferencia());
        fila.createCell(1).setCellValue(unidad.getArticulo().getDescripcion());
        fila.createCell(2).setCellValue(unidad.getArticulo().getPrecioUnitario());
        fila.createCell(3).setCellValue(unidad.getArticulo().getIva());
        fila.createCell(4).setCellValue(unidad.getArticulo().getCodCategoria());
        fila.createCell(5).setCellValue(unidad.getArticulo().getCodSubcategoria());
        fila.createCell(6).setCellValue(unidad.getArticulo().getFabricante());
        fila.createCell(7).setCellValue(unidad.getArticulo().getModelo());
        
        indiceFila++;
		indiceFila++;
		
		String[] encabezadosOficina = { "Dirección", "Localidad", "Provincia", "País", "Código postal"};
		
		fila = hoja.createRow(indiceFila);
		XSSFCell celda3 = fila.createCell(0);
		celda3.setCellValue("OFICINA DE SALIDA");
		celda3.setCellStyle(headerStyle);
		indiceFila++;
		indiceFila++;
		
		fila = hoja.createRow(indiceFila);
		for (int i = 0; i < encabezadosOficina.length; i++) {
			String encabezadoL = encabezadosOficina[i];
			XSSFCell celda1 = fila.createCell(i);
			celda1.setCellValue(encabezadoL);
			celda1.setCellStyle(headerStyleLinea);
		}
		indiceFila++;
		fila = hoja.createRow(indiceFila);
		fila.createCell(0).setCellValue(unidad.getOficina().getDireccion());
		fila.createCell(1).setCellValue(unidad.getOficina().getLocalidad());
		
		if(unidad.getOficina().getProvincia()!=null) {
			fila.createCell(2).setCellValue(unidad.getOficina().getProvincia());
		}else {
			fila.createCell(2).setCellValue("-");
		}
		
		fila.createCell(3).setCellValue(unidad.getOficina().getPais());
		
		if(unidad.getOficina().getProvincia()!=null) {
			fila.createCell(4).setCellValue(unidad.getOficina().getCodigoPostal());
		}else {
			fila.createCell(4).setCellValue("-");
		}
		
		List<AsignacionEntity> listaAsignaciónEntity = this.asignacionRepository.findByCodUnidad(id);

		if(listaAsignaciónEntity.size()>0) {
			indiceFila++;
			indiceFila++;
			
			String[] encabezadosAsignacion = { "Fecha inicio", "Fecha fin", "Empleado"};
			
			fila = hoja.createRow(indiceFila);
			celda = fila.createCell(0);
			celda.setCellValue("ASIGNACIONES");
			celda.setCellStyle(headerStyle);
			indiceFila++;
			indiceFila++;
			
			fila = hoja.createRow(indiceFila);
			for (int i = 0; i < encabezadosAsignacion.length; i++) {
				String encabezadoL = encabezadosAsignacion[i];
				XSSFCell celda1 = fila.createCell(i);
				celda1.setCellValue(encabezadoL);
				celda1.setCellStyle(headerStyleLinea);
			}
			indiceFila++;
			
			for (AsignacionEntity asignacion : listaAsignaciónEntity) {
				fila = hoja.createRow(indiceFila);
				
				fila.createCell(0).setCellValue(asignacion.getFechaInicio());
				fila.getCell(0).setCellStyle(dateCellStyle);
				
				if(asignacion.getFechaFin()!= null) {
					fila.createCell(1).setCellValue(asignacion.getFechaFin());
					fila.getCell(1).setCellStyle(dateCellStyle);
				}else {
					fila.createCell(1).setCellValue("-");
				}
				
				fila.createCell(2).setCellValue(asignacion.getEmpleado().getDni() + ": " + asignacion.getEmpleado().getNombre()+ " " + asignacion.getEmpleado().getApellidos());
				indiceFila++;
			}
		}
		
		for (int i = 0; i < 8; i++) {
			hoja.autoSizeColumn(i);
			hoja.setDefaultColumnStyle(i, cellStyle);
		}
		
		// Convertir el workbook a bytes
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		byte[] bytes = outputStream.toByteArray();
		outputStream.close();
		workbook.close();

		return bytes;
	}
}
