package com.tfg.inventariado.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventario")
@NoArgsConstructor
@AllArgsConstructor
@Data
@IdClass(InventarioEntityID.class) 
public class InventarioEntity {

	@Column(name="cod_articulo")
	@Id
	private Integer codArticulo;
	
	@Column(name="id_oficina")
	@Id
	private Integer idOficina;
	
	@Column(name="stock", nullable = false)
	private Integer stock;
	
	@ManyToOne
	@JoinColumn(name="cod_articulo", referencedColumnName="codigo_articulo", insertable = false, updatable = false)
	//Primero el campo de esta tabla y luego a la que referencia
	private ArticuloEntity articulo;
	
	@ManyToOne
	@JoinColumn(name="id_oficina", referencedColumnName="id_oficina", insertable = false, updatable = false)
	//Primero el campo de esta tabla y luego a la que referencia
	private OficinaEntity oficina;
}
