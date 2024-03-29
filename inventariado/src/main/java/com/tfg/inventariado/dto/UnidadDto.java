package com.tfg.inventariado.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UnidadDto {

	private Integer codigoInterno;
	private String codEstado;
	private Integer numeroPedido;
	private Integer idSalida;
	private Integer idOficina;
	private Integer codArticulo;
	
	private EstadoDto estado;
	private PedidoDto pedido;
	private SalidaDto salida;
	private OficinaDto oficina;
	private ArticuloDto articulo;
}
