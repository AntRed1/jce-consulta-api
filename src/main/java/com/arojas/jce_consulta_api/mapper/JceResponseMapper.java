package com.arojas.jce_consulta_api.mapper;

import org.springframework.stereotype.Component;

import com.arojas.jce_consulta_api.dto.CedulaResultDto;
import com.arojas.jce_consulta_api.dto.CedulaResultDto.CedulaValidationInfo;
import com.arojas.jce_consulta_api.dto.JceResponseDto;
import com.arojas.jce_consulta_api.util.CedulaValidationUtils.CedulaInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mapper para convertir respuestas JCE a DTOs de resultado
 * Incluye mapeo de información de validación de cédulas
 *
 * @author arojas
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JceResponseMapper {

	/**
	 * Convierte JceResponseDto y CedulaInfo a CedulaResultDto
	 *
	 * @param jceResponse Respuesta del servicio JCE
	 * @param cedulaInfo  Información procesada de la cédula
	 * @return DTO de resultado mapeado
	 */
	public CedulaResultDto toResultDto(JceResponseDto jceResponse, CedulaInfo cedulaInfo) {
		if (jceResponse == null) {
			log.warn("JceResponseDto es null, creando resultado vacío");
			return createEmptyResult(cedulaInfo);
		}

		CedulaResultDto.CedulaResultDtoBuilder builder = CedulaResultDto.builder()
				// Información básica
				.cedula(cedulaInfo != null ? cedulaInfo.cedulaFormateada() : null)

				// Datos personales
				.nombres(cleanString(jceResponse.getNombres()))
				.apellido1(cleanString(jceResponse.getApellido1()))
				.apellido2(cleanString(jceResponse.getApellido2()))
				.nombreCompleto(jceResponse.getNombreCompleto())

				// Datos demográficos
				.fechaNacimiento(cleanString(jceResponse.getFechaNacimiento()))
				.lugarNacimiento(cleanString(jceResponse.getLugarNacimiento()))
				.sexo(cleanString(jceResponse.getSexo()))
				.estadoCivil(cleanString(jceResponse.getEstadoCivil()))

				// Información de cédula
				.municipioCedula(cleanString(jceResponse.getMunicipioCedula()))
				.secuenciaCedula(cleanString(jceResponse.getSecuenciaCedula()))

				// Información de nacionalidad
				.codigoNacionalidad(cleanString(jceResponse.getCodigoNacionalidad()))
				.descripcionNacionalidad(cleanString(jceResponse.getDescripcionNacionalidad()))

				// Información de documento
				.fechaExpiracion(cleanString(jceResponse.getFechaExpiracion()))
				.categoria(cleanString(jceResponse.getCategoria()))
				.descripcionCategoria(cleanString(jceResponse.getDescripcionCategoria()))
				.estatus(cleanString(jceResponse.getEstatus()))
				.fotoUrl(cleanString(jceResponse.getFotoUrl()));

		// Agregar información de validación si está disponible
		if (cedulaInfo != null) {
			builder.validationInfo(createValidationInfo(cedulaInfo));
		}

		CedulaResultDto result = builder.build();

		log.debug("Mapeado exitoso de JceResponseDto a CedulaResultDto para cédula: {}",
				cedulaInfo != null ? cedulaInfo.cedulaFormateada() : "N/A");

		return result;
	}

	/**
	 * Convierte solo JceResponseDto a CedulaResultDto (sin información de
	 * validación)
	 * 
	 * @param jceResponse Respuesta del servicio JCE
	 * @return DTO de resultado mapeado
	 */
	public CedulaResultDto toResultDto(JceResponseDto jceResponse) {
		return toResultDto(jceResponse, null);
	}

	/**
	 * Crea un resultado vacío con información de validación
	 * 
	 * @param cedulaInfo Información de la cédula
	 * @return DTO de resultado vacío
	 */
	public CedulaResultDto createEmptyResult(CedulaInfo cedulaInfo) {
		CedulaResultDto.CedulaResultDtoBuilder builder = CedulaResultDto.builder()
				.success(false)
				.message("No se encontraron datos");

		if (cedulaInfo != null) {
			builder.cedula(cedulaInfo.cedulaFormateada())
					.validationInfo(createValidationInfo(cedulaInfo));
		}

		return builder.build();
	}

	/**
	 * Actualiza un CedulaResultDto existente con información de validación
	 * 
	 * @param result     DTO de resultado existente
	 * @param cedulaInfo Información de validación de la cédula
	 * @return DTO actualizado
	 */
	public CedulaResultDto enrichWithValidationInfo(CedulaResultDto result, CedulaInfo cedulaInfo) {
		if (result == null || cedulaInfo == null) {
			return result;
		}

		result.setValidationInfo(createValidationInfo(cedulaInfo));

		// Asegurar que la cédula esté formateada correctamente
		if (result.getCedula() == null || result.getCedula().trim().isEmpty()) {
			result.setCedula(cedulaInfo.cedulaFormateada());
		}

		return result;
	}

	// ================= MÉTODOS PRIVADOS =================

	/**
	 * Limpia y valida strings, retorna null si está vacío
	 */
	private String cleanString(String input) {
		if (input == null || input.trim().isEmpty()) {
			return null;
		}
		return input.trim();
	}

	/**
	 * Crea información de validación desde CedulaInfo
	 */
	private CedulaValidationInfo createValidationInfo(CedulaInfo cedulaInfo) {
		if (cedulaInfo == null) {
			return null;
		}

		return CedulaValidationInfo.builder()
				.digitoVerificadorValido(cedulaInfo.digitoVerificadorValido())
				.formatoValido(true) // Si llegó hasta aquí, el formato es válido
				.municipioCodigo(cedulaInfo.municipio())
				.secuencia(cedulaInfo.secuencia())
				.digitoVerificador(cedulaInfo.digitoVerificador())
				.cedulaFormateada(cedulaInfo.cedulaFormateada())
				.cedulaNormalizada(cedulaInfo.cedulaCompleta())
				.build();
	}

	/**
	 * Convierte JceResponseDto a JSON string usando el mapper interno
	 * 
	 * @param jceResponse  Respuesta JCE
	 * @param jsonResponse JSON string de la respuesta
	 * @param cedulaInfo   Información de la cédula
	 * @return DTO de resultado con JSON incluido
	 */
	public CedulaResultDto toResultDtoWithJson(JceResponseDto jceResponse, String jsonResponse, CedulaInfo cedulaInfo) {
		CedulaResultDto result = toResultDto(jceResponse, cedulaInfo);

		if (result != null && jsonResponse != null) {
			result.setJsonResponse(jsonResponse);
		}

		return result;
	}

	/**
	 * Valida si un JceResponseDto tiene datos mínimos requeridos
	 * 
	 * @param jceResponse Respuesta a validar
	 * @return true si tiene datos mínimos válidos
	 */
	public boolean hasValidData(JceResponseDto jceResponse) {
		return jceResponse != null && jceResponse.hasValidData();
	}

	/**
	 * Obtiene resumen de la información mapeada para logs
	 * 
	 * @param result DTO de resultado
	 * @return String con resumen
	 */
	public String getSummary(CedulaResultDto result) {
		if (result == null) {
			return "Resultado nulo";
		}

		StringBuilder summary = new StringBuilder();
		summary.append("Cédula: ").append(result.getCedula())
				.append(", Éxito: ").append(result.isSuccess())
				.append(", Datos: ").append(result.hasValidPersonData() ? "Válidos" : "Inválidos");

		if (result.hasValidPersonData()) {
			summary.append(", Nombre: ").append(result.getNombreCompleto());
		}

		if (result.getValidationInfo() != null) {
			summary.append(", DV Válido: ").append(result.getValidationInfo().getDigitoVerificadorValido());
		}

		return summary.toString();
	}
}