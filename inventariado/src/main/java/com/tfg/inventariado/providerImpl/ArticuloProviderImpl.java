package com.tfg.inventariado.providerImpl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tfg.inventariado.dto.ArticuloDto;
import com.tfg.inventariado.dto.MessageResponseDto;
import com.tfg.inventariado.entity.ArticuloEntity;
import com.tfg.inventariado.provider.ArticuloProvider;
import com.tfg.inventariado.provider.CategoriaProvider;
import com.tfg.inventariado.provider.SubcategoriaProvider;
import com.tfg.inventariado.repository.ArticuloRepository;

@Service
public class ArticuloProviderImpl implements ArticuloProvider {
	
	@Autowired
	private ArticuloRepository articuloRepository;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private SubcategoriaProvider subcategoriaProvider;

	@Autowired
	private CategoriaProvider categoriaProvider;
	
	@Override
	public ArticuloDto convertToMapDto(ArticuloEntity articulo) {
		return modelMapper.map(articulo, ArticuloDto.class);
	}

	@Override
	public ArticuloEntity convertToMapEntity(ArticuloDto articulo) {
		return modelMapper.map(articulo, ArticuloEntity.class);
	}

	@Override
	public List<ArticuloDto> listAllArticulo() {
		List<ArticuloEntity> listaArticuloEntity = this.articuloRepository.findAll();
		return listaArticuloEntity.stream().map(this::convertToMapDto).collect(Collectors.toList());
	}

	@Override
	public MessageResponseDto<String> addArticulo(ArticuloDto articulo) {
		if(articuloExisteByID(articulo.getCodigoArticulo())) {
			return MessageResponseDto.fail("El artículo ya existe");
		}
		if(articulo.getDescripcion() == null || articulo.getDescripcion().isEmpty()) {
			return MessageResponseDto.fail("La descripción es obligatoria");
		}
		if(articulo.getPrecioUnitario() <= 0) {
			return MessageResponseDto.fail("El precio debe ser mayor que 0");
		}
		if(articulo.getIva() <= 0) {
			return MessageResponseDto.fail("El iva debe ser mayor que 0");
		}
		if(articulo.getReferencia() == null || articulo.getReferencia().isEmpty()) {
			return MessageResponseDto.fail("La referencia es obligatoria");
		}
		if(articulo.getCodCategoria() == null || articulo.getCodCategoria().isEmpty()) {
			return MessageResponseDto.fail("La categoría es obligatoria");
		}
		if(articulo.getCodSubcategoria() == null || articulo.getCodSubcategoria().isEmpty()) {
			return MessageResponseDto.fail("La subcategoría es obligatoria");
		}
		if(articulo.getFabricante() == null || articulo.getFabricante().isEmpty()) {
			return MessageResponseDto.fail("El fabricante es obligatorio");
		}
		if(!this.categoriaProvider.categoriaExisteByCodigo(articulo.getCodCategoria())) {
			return MessageResponseDto.fail("No se puede añadir el artículo porque la categoria no existe");
		}
		if(!this.subcategoriaProvider.subcategoriaExisteByID(articulo.getCodCategoria(), articulo.getCodSubcategoria())) {
			return MessageResponseDto.fail("No se puede añadir el artículo porque la subcategoria no existe");
		}
		ArticuloEntity newArticulo = this.convertToMapEntity(articulo);
		newArticulo = this.articuloRepository.save(newArticulo);
		return MessageResponseDto.success("Artículo añadido con éxito");
	}

	@Override
	public MessageResponseDto<String> editArticulo(ArticuloDto articulo, Integer articuloId) {
		Optional<ArticuloEntity> optionalArticulo = this.articuloRepository.findById(articuloId);
		if(!optionalArticulo.isPresent()) {
			return MessageResponseDto.fail("El artículo que se desea editar no existe");
		}
		ArticuloEntity articuloToUpdate = optionalArticulo.get();
		this.actualizarCampos(articuloToUpdate, articulo);
		this.articuloRepository.save(articuloToUpdate);
		return MessageResponseDto.success("Artículo editado con éxito");
	}
	
	private void actualizarCampos(ArticuloEntity articulo, ArticuloDto articuloToUpdate) {
		if(StringUtils.isNotBlank(articuloToUpdate.getDescripcion())) {
			articulo.setDescripcion(articuloToUpdate.getDescripcion());
		}
		if (Double.compare(articuloToUpdate.getPrecioUnitario(), 0.0) != 0) {
			articulo.setPrecioUnitario(articuloToUpdate.getPrecioUnitario());
		}
		if(StringUtils.isNotBlank(articuloToUpdate.getReferencia())) {
			articulo.setReferencia(articuloToUpdate.getReferencia());
		}
		if(StringUtils.isNotBlank(articuloToUpdate.getCodCategoria())) {
			articulo.setCodCategoria(articuloToUpdate.getCodCategoria());
		}
		if(StringUtils.isNotBlank(articuloToUpdate.getCodSubcategoria())) {
			articulo.setCodSubcategoria(articuloToUpdate.getCodSubcategoria());
		}
		if (Double.compare(articuloToUpdate.getIva(), 0.0) != 0) {
			articulo.setIva(articuloToUpdate.getIva());
		}
		if(StringUtils.isNotBlank(articuloToUpdate.getFabricante())) {
			articulo.setFabricante(articuloToUpdate.getFabricante());
		}
		if(StringUtils.isNotBlank(articuloToUpdate.getModelo())) {
			articulo.setModelo(articuloToUpdate.getModelo());
		}
		
	}

	@Override
	public MessageResponseDto<ArticuloDto> getArticuloById(Integer articuloId) {
		Optional<ArticuloEntity> optionalArticulo = this.articuloRepository.findById(articuloId);
		if(!optionalArticulo.isPresent()) {
			return MessageResponseDto.fail("El artículo no existe");
		}
		ArticuloDto articuloDto = this.convertToMapDto(optionalArticulo.get());
		return MessageResponseDto.success(articuloDto);
	}

	@Override
	public MessageResponseDto<List<ArticuloDto>> listArticulosByCategoria(String codigoCategoria) {
		if(!this.categoriaProvider.categoriaExisteByCodigo(codigoCategoria)) {
			return MessageResponseDto.fail("La categoria no existe");
		}
		List<ArticuloEntity> listaArticuloEntity = this.articuloRepository.findByCodCategoria(codigoCategoria);
		List<ArticuloDto> listaArticuloDto = listaArticuloEntity.stream().map(this::convertToMapDto).collect(Collectors.toList());
		return MessageResponseDto.success(listaArticuloDto);
	}

	@Override
	public MessageResponseDto<List<ArticuloDto>> listArticulosBySubcategoria(String codigoCategoria, String codigoSubcategoria) {
		if(!this.subcategoriaProvider.subcategoriaExisteByID(codigoCategoria, codigoSubcategoria)) {
			return MessageResponseDto.fail("La subcategoria no existe");
		}
		List<ArticuloEntity> listaArticuloEntity = this.articuloRepository.findByCodCategoriaAndCodSubcategoria(codigoCategoria,codigoSubcategoria);
		List<ArticuloDto> listaArticuloDto = listaArticuloEntity.stream().map(this::convertToMapDto).collect(Collectors.toList());
		return MessageResponseDto.success(listaArticuloDto);
		
	}

	@Override
	public boolean articuloExisteByID(Integer articuloId) {
		Optional<ArticuloEntity> optionalArticulo = this.articuloRepository.findById(articuloId);
		return optionalArticulo.isPresent() ? true : false;
	}

}
