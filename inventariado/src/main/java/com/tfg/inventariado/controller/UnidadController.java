package com.tfg.inventariado.controller;

import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.inventariado.dto.ArticuloDto;
import com.tfg.inventariado.dto.MessageResponseDto;
import com.tfg.inventariado.dto.MessageResponseListDto;
import com.tfg.inventariado.dto.PedidoDto;
import com.tfg.inventariado.dto.UnidadDto;
import com.tfg.inventariado.dto.UnidadFilterDto;
import com.tfg.inventariado.provider.UnidadProvider;

@RestController
@RequestMapping("/unidad")
@CrossOrigin(origins = "http://localhost:4200")
public class UnidadController {

	@Autowired
	private UnidadProvider unidadProvider;
	
	@PostMapping("/add")
	public ResponseEntity<MessageResponseDto<?>> agregarUnidad(@RequestBody @Valid UnidadDto unidadRequest) {
		
		MessageResponseDto<Integer> messageResponse = this.unidadProvider.addUnidad(unidadRequest);

		if (messageResponse.isSuccess()) {
			return ResponseEntity.status(HttpStatus.OK).body(messageResponse);
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(MessageResponseDto.fail(messageResponse.getError()));
		}
	}
	
	@GetMapping("/listAll")
	public ResponseEntity<List<UnidadDto>> listarUnidades() {
		List<UnidadDto> listaDto = this.unidadProvider.listAllUnidades();
		return new ResponseEntity<List<UnidadDto>>(listaDto, HttpStatus.OK);
	}
	
	@PutMapping("/editar/{id}")
	public ResponseEntity<MessageResponseDto<String>> editUnidadById( @PathVariable("id") Integer id,
			 @RequestBody @Valid UnidadDto unidadUpadate) {
		MessageResponseDto<String> messageResponse = this.unidadProvider.editUnidad(unidadUpadate, id);

		if (messageResponse.isSuccess()) {
			return ResponseEntity.status(HttpStatus.OK).body(messageResponse);
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(MessageResponseDto.fail(messageResponse.getError()));
		}
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<MessageResponseDto<UnidadDto>> getUnidadById(@PathVariable("id") Integer id){
			MessageResponseDto<UnidadDto> unidad = this.unidadProvider.getUnidadById( id);
			if(unidad.isSuccess()) {
				return ResponseEntity.status(HttpStatus.OK).body(unidad);
			}else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(MessageResponseDto.fail(unidad.getError()));
			}
	}
	
	@GetMapping("/listByEstado/{cod}")
	public ResponseEntity<MessageResponseDto<List<UnidadDto>>> listUnidadByEstado(@PathVariable("cod") String codEstado) {
		MessageResponseDto<List<UnidadDto>> listaDto = this.unidadProvider.listUnidadByEstado(codEstado);
		if(listaDto.isSuccess()) {
			return ResponseEntity.status(HttpStatus.OK).body(listaDto);
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(MessageResponseDto.fail(listaDto.getError()));
		}
	}
	
	@GetMapping("/listDisponibles")
	public ResponseEntity<MessageResponseDto<List<UnidadDto>>> listUnidadDisponibles() {
		MessageResponseDto<List<UnidadDto>> listaDto = this.unidadProvider.listUnidadDisponibles();
		if(listaDto.isSuccess()) {
			return ResponseEntity.status(HttpStatus.OK).body(listaDto);
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(MessageResponseDto.fail(listaDto.getError()));
		}
	}
	
	@GetMapping("/listDisponibles/{idOf}")
	public ResponseEntity<MessageResponseDto<List<UnidadDto>>> listUnidadDisponiblesByOficina(@PathVariable("idOf") Integer id) {
		MessageResponseDto<List<UnidadDto>> listaDto = this.unidadProvider.listUnidadDisponiblesByOficina(id);
		if(listaDto.isSuccess()) {
			return ResponseEntity.status(HttpStatus.OK).body(listaDto);
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(MessageResponseDto.fail(listaDto.getError()));
		}
	}
	
	@GetMapping("/listNODisponibles")
	public ResponseEntity<MessageResponseDto<List<UnidadDto>>> listUnidadNODisponibles() {
		MessageResponseDto<List<UnidadDto>> listaDto = this.unidadProvider.listUnidadNODisponibles();
		if(listaDto.isSuccess()) {
			return ResponseEntity.status(HttpStatus.OK).body(listaDto);
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(MessageResponseDto.fail(listaDto.getError()));
		}
	}
	
	@GetMapping("/listNODisponibles/{idOf}")
	public ResponseEntity<MessageResponseDto<List<UnidadDto>>> listUnidadNODisponiblesByOficina(@PathVariable("idOf") Integer id) {
		MessageResponseDto<List<UnidadDto>> listaDto = this.unidadProvider.listUnidadNODisponiblesByOficina(id);
		if(listaDto.isSuccess()) {
			return ResponseEntity.status(HttpStatus.OK).body(listaDto);
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(MessageResponseDto.fail(listaDto.getError()));
		}
	}
	
	@GetMapping("/listByOficina/{idOf}")
	public ResponseEntity<MessageResponseDto<List<UnidadDto>>> listUnidadesByOficina(@PathVariable("idOf") Integer id) {
		MessageResponseDto<List<UnidadDto>> listaDto = this.unidadProvider.listUnidadesByOficina(id);
		if(listaDto.isSuccess()) {
			return ResponseEntity.status(HttpStatus.OK).body(listaDto);
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(MessageResponseDto.fail(listaDto.getError()));
		}
	}
	
	@GetMapping("/listByCodArt/{idArt}")
	public ResponseEntity<MessageResponseDto<List<UnidadDto>>> listUnidadByArticulo(@PathVariable("idArt") Integer id) {
		MessageResponseDto<List<UnidadDto>> listaDto = this.unidadProvider.listUnidadByArticulo(id);
		if(listaDto.isSuccess()) {
			return ResponseEntity.status(HttpStatus.OK).body(listaDto);
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(MessageResponseDto.fail(listaDto.getError()));
		}
	}
	
	@PutMapping("/salida/{idSal}/{idUn}")
	public ResponseEntity<MessageResponseDto<String>> darSalidaUnidad( @PathVariable("idSal") Integer idS, @PathVariable("idUn") Integer idU) {
		MessageResponseDto<String> messageResponse = this.unidadProvider.darSalidaUnidad(idS, idU);

		if (messageResponse.isSuccess()) {
			return ResponseEntity.status(HttpStatus.OK).body(messageResponse);
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(MessageResponseDto.fail(messageResponse.getError()));
		}
	}
	
	@PostMapping("/listAllPag")
	public ResponseEntity<MessageResponseListDto<List<UnidadDto>>> listarUnidadPag(@RequestParam(value = "limit", required = false) Integer limit,
		    @RequestParam(value = "skip", required = false) Integer skip, @RequestBody UnidadFilterDto filtros) {
		MessageResponseListDto<List<UnidadDto>> listaDto = this.unidadProvider.listAllUnidadesSkipLimit(skip,limit,filtros);
		return new ResponseEntity<MessageResponseListDto<List<UnidadDto>>>(listaDto, HttpStatus.OK);
	}
	
	@GetMapping("/listArtDiponiblesByOficina")
	public ResponseEntity<List<ArticuloDto>> listaArticulosDisponiblesEnInventarioParaRegistrarUnidadesByOficina(@RequestParam(value = "idOficina", required = true) Integer idOficina) {
		List<ArticuloDto> listaDto = this.unidadProvider.listaArticulosDisponiblesEnInventarioParaRegistrarUnidadesByOficina(idOficina);
		return new ResponseEntity<List<ArticuloDto>>(listaDto, HttpStatus.OK);
	}
	
	@GetMapping("/listPedidosDispParaUdsByOficinaAndArt")
	public ResponseEntity<MessageResponseDto<List<PedidoDto>>> pedidosDisponiblesByOficinaAndArticulo(@RequestParam(value = "idOficina", required = true) Integer idOficina,@RequestParam(value = "codArticulo", required = true) Integer codArticulo) {
		MessageResponseDto<List<PedidoDto>> messageResponse = this.unidadProvider.pedidosDisponiblesByOficinaAndArticulo(idOficina,codArticulo);

		if (messageResponse.isSuccess()) {
			return ResponseEntity.status(HttpStatus.OK).body(messageResponse);
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(MessageResponseDto.fail(messageResponse.getError()));
		}
	}
	
	@GetMapping("/listDisponiblesSinAsignar/{idOf}")
	public ResponseEntity<MessageResponseDto<List<UnidadDto>>> listUnidadDisponiblesSinAsignarByOficina(@PathVariable("idOf") Integer id) {
		MessageResponseDto<List<UnidadDto>> listaDto = this.unidadProvider.listUnidadDisponiblesSinAsignarByOficina(id);
		if(listaDto.isSuccess()) {
			return ResponseEntity.status(HttpStatus.OK).body(listaDto);
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(MessageResponseDto.fail(listaDto.getError()));
		}
	}
	
	@GetMapping("/asignada/{codInterno}")
	public ResponseEntity<MessageResponseDto<Boolean>> estaAsignada(@PathVariable("codInterno") Integer codInterno) {
		MessageResponseDto<Boolean> res = this.unidadProvider.estaAsignada(codInterno);
		if(res.isSuccess()) {
			return ResponseEntity.status(HttpStatus.OK).body(res);
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(MessageResponseDto.fail(res.getError()));
		}
	}
	
	@PostMapping("/descargarExcelById")
	public byte[] descargarExcelUnidadById(@RequestParam(value = "id", required = true) Integer id)throws IOException{
		try {
			return this.unidadProvider.descargarExcelUnidadById(id);
		} catch (IOException e) {
			throw e;
		}
	}
	
}
