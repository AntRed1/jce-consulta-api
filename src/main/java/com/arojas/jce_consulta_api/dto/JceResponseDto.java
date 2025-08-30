/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author arojas
 *         * DTO para la respuesta del servicio JCE
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class JceResponseDto {

	@XmlElement(name = "nombres")
	@JsonProperty("nombres")
	private String nombres;

	@XmlElement(name = "apellido1")
	@JsonProperty("apellido1")
	private String apellido1;

	@XmlElement(name = "apellido2")
	@JsonProperty("apellido2")
	private String apellido2;

	@XmlElement(name = "fecha_nac")
	@JsonProperty("fechaNacimiento")
	private String fechaNacimiento;

	@XmlElement(name = "lugar_nac")
	@JsonProperty("lugarNacimiento")
	private String lugarNacimiento;

	@XmlElement(name = "sexo")
	@JsonProperty("sexo")
	private String sexo;

	@XmlElement(name = "est_civil")
	@JsonProperty("estadoCivil")
	private String estadoCivil;

	@XmlElement(name = "mun_ced")
	@JsonProperty("municipioCedula")
	private String municipioCedula;

	@XmlElement(name = "seq_ced")
	@JsonProperty("secuenciaCedula")
	private String secuenciaCedula;

	@XmlElement(name = "cod_nacion")
	@JsonProperty("codigoNacionalidad")
	private String codigoNacionalidad;

	@XmlElement(name = "desc_nacionalidad")
	@JsonProperty("descripcionNacionalidad")
	private String descripcionNacionalidad;

	@XmlElement(name = "fecha_expiracion")
	@JsonProperty("fechaExpiracion")
	private String fechaExpiracion;

	@XmlElement(name = "categoria")
	@JsonProperty("categoria")
	private String categoria;

	@XmlElement(name = "desc_categoria")
	@JsonProperty("descripcionCategoria")
	private String descripcionCategoria;

	@XmlElement(name = "estatus")
	@JsonProperty("estatus")
	private String estatus;

	@XmlElement(name = "fotourl")
	@JsonProperty("fotoUrl")
	private String fotoUrl;

	/**
	 * Obtiene el nombre completo concatenado
	 */
	public String getNombreCompleto() {
		return String.join(" ",
				nombres != null ? nombres : "",
				apellido1 != null ? apellido1 : "",
				apellido2 != null ? apellido2 : "").trim();
	}

	/**
	 * Verifica si la respuesta contiene datos válidos
	 */
	public boolean hasValidData() {
		return nombres != null && !nombres.trim().isEmpty() &&
				apellido1 != null && !apellido1.trim().isEmpty();
	}
}
