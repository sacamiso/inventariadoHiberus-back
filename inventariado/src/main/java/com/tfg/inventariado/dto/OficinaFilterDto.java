package com.tfg.inventariado.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OficinaFilterDto {
	private Integer codigoPostal;
	private String direccion;
	private String localidad;
	private String provincia;
	private String pais;
}
