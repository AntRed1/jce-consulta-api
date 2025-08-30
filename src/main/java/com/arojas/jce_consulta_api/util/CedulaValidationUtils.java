/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.util;

import java.io.StringReader;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.arojas.jce_consulta_api.dto.JceResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 *         * Utilidades unificadas para validación y procesamiento de cédulas
 *         dominicanas
 *         Combina funcionalidades de CedulaUtils y DominicanValidationUtils
 *         Incluye conversión de XML a JSON para respuestas del servicio JCE
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class CedulaValidationUtils {

	private final ObjectMapper objectMapper;

	@SuppressWarnings("unused")
	private static final Pattern CEDULA_PATTERN = Pattern.compile("^\\d{3}-?\\d{7}-?\\d{1}$");
	private static final Pattern CEDULA_DIGITS_PATTERN = Pattern.compile("\\d{11}");
	private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{10}");
	private static final int CEDULA_LENGTH = 11;

	/**
	 * Valida formato de cédula dominicana
	 */
	public boolean isValidFormat(String cedula) {
		if (cedula == null || cedula.trim().isEmpty()) {
			return false;
		}

		String cleanCedula = cleanCedula(cedula);

		if (cleanCedula.length() != CEDULA_LENGTH) {
			return false;
		}

		// Verificar que no sean todos ceros o todos el mismo dígito
		if (cleanCedula.matches("0{11}") || cleanCedula.matches("(\\d)\\1{10}")) {
			return false;
		}

		return CEDULA_DIGITS_PATTERN.matcher(cleanCedula).matches();
	}

	/**
	 * Método legacy mantenido por compatibilidad
	 */
	public boolean isValidCedulaFormat(String cedula) {
		return isValidFormat(cedula);
	}

	/**
	 * Normaliza la cédula removiendo guiones y espacios
	 */
	public String normalize(String cedula) {
		if (cedula == null) {
			return null;
		}
		return cedula.replaceAll("[\\s-]", "").trim();
	}

	/**
	 * Obtiene solo los dígitos de una cédula
	 */
	public String cleanCedula(String cedula) {
		if (cedula == null) {
			return null;
		}
		return cedula.replaceAll("\\D", "");
	}

	/**
	 * Formatea la cédula con guiones
	 */
	public String format(String cedula) {
		String normalized = cleanCedula(cedula);
		if (normalized == null || normalized.length() != CEDULA_LENGTH) {
			return cedula; // Retornar original si no es válida
		}
		return normalized.substring(0, 3) + "-" +
				normalized.substring(3, 10) + "-" +
				normalized.substring(10, 11);
	}

	/**
	 * Método legacy para formatear cédula
	 */
	public String formatCedula(String cedula) {
		return format(cedula);
	}

	/**
	 * Valida formato de teléfono dominicano
	 */
	public boolean isValidPhoneFormat(String phone) {
		if (phone == null || phone.trim().isEmpty()) {
			return false;
		}
		String cleanPhone = phone.replaceAll("\\D", "");
		return PHONE_PATTERN.matcher(cleanPhone).matches();
	}

	/**
	 * Valida completamente una cédula dominicana
	 */
	public void validateCedula(String cedula) {
		if (cedula == null || cedula.trim().isEmpty()) {
			throw new IllegalArgumentException("La cédula es requerida");
		}

		String normalizedCedula = cleanCedula(cedula);

		if (normalizedCedula.length() != CEDULA_LENGTH) {
			throw new IllegalArgumentException("La cédula debe tener 11 dígitos");
		}

		if (!normalizedCedula.matches("\\d{11}")) {
			throw new IllegalArgumentException("La cédula solo debe contener números");
		}

		// Validación: no puede comenzar con 000
		if (normalizedCedula.startsWith("000")) {
			throw new IllegalArgumentException("La cédula no puede comenzar con 000");
		}

		// Validación: no puede ser todos ceros o todos iguales
		if (normalizedCedula.matches("0{11}") || normalizedCedula.matches("(\\d)\\1{10}")) {
			throw new IllegalArgumentException("La cédula no puede ser todos ceros o todos el mismo dígito");
		}

		log.debug("Cédula validada exitosamente: {}", maskCedula(normalizedCedula));
	}

	/**
	 * Extrae el código de municipio de la cédula (3 primeros dígitos)
	 */
	public String getMunicipioCode(String cedula) {
		String normalized = cleanCedula(cedula);
		validateCedula(normalized);
		return normalized.substring(0, 3);
	}

	/**
	 * Extrae la secuencia de la cédula (7 dígitos del medio)
	 */
	public String getSecuencia(String cedula) {
		String normalized = cleanCedula(cedula);
		validateCedula(normalized);
		return normalized.substring(3, 10);
	}

	/**
	 * Extrae el dígito verificador de la cédula (último dígito)
	 */
	public String getDigitoVerificador(String cedula) {
		String normalized = cleanCedula(cedula);
		validateCedula(normalized);
		return normalized.substring(10, 11);
	}

	/**
	 * Enmascara la cédula para logs (muestra solo los primeros 3 y últimos 2
	 * dígitos)
	 */
	public String maskCedula(String cedula) {
		if (cedula == null || cedula.length() < 5) {
			return "***-*****-*";
		}
		String normalized = normalize(cedula);
		if (normalized.length() != CEDULA_LENGTH) {
			return "***-*****-*";
		}
		return normalized.substring(0, 3) + "-*****-" + normalized.substring(10, 11);
	}

	/**
	 * Valida el dígito verificador usando el algoritmo estándar dominicano
	 */
	public boolean isValidDigitoVerificador(String cedula) {
		String normalized = normalize(cedula);
		if (normalized.length() != CEDULA_LENGTH) {
			return false;
		}

		try {
			int[] multiplicadores = { 1, 2, 1, 2, 1, 2, 1, 2, 1, 2 };
			int suma = 0;

			for (int i = 0; i < 10; i++) {
				int digito = Character.getNumericValue(normalized.charAt(i));
				int producto = digito * multiplicadores[i];

				// Si el producto es mayor a 9, sumar los dígitos
				if (producto > 9) {
					producto = (producto / 10) + (producto % 10);
				}
				suma += producto;
			}

			int digitoCalculado = (10 - (suma % 10)) % 10;
			int digitoReal = Character.getNumericValue(normalized.charAt(10));

			return digitoCalculado == digitoReal;

		} catch (Exception e) {
			log.error("Error validando dígito verificador para cédula: {}", maskCedula(cedula), e);
			return false;
		}
	}

	/**
	 * Convierte respuesta XML del servicio JCE a JSON
	 * 
	 * @param xmlResponse Respuesta XML del servicio JCE
	 * @return JSON string de la respuesta convertida
	 * @throws JAXBException           si hay error parseando el XML
	 * @throws JsonProcessingException si hay error generando el JSON
	 */
	public String convertXmlToJson(String xmlResponse) throws JAXBException, JsonProcessingException {
		if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
			log.warn("Respuesta XML vacía recibida");
			return "{}";
		}

		try {
			// Parsear XML a objeto DTO
			JAXBContext jaxbContext = JAXBContext.newInstance(JceResponseDto.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			JceResponseDto responseDto = (JceResponseDto) unmarshaller.unmarshal(new StringReader(xmlResponse));

			// Convertir objeto a JSON
			String jsonResponse = objectMapper.writeValueAsString(responseDto);

			log.debug("XML convertido exitosamente a JSON");
			return jsonResponse;

		} catch (JAXBException e) {
			log.error("Error parseando respuesta XML: {}", e.getMessage());
			log.debug("XML problemático: {}",
					xmlResponse.length() > 500 ? xmlResponse.substring(0, 500) + "..." : xmlResponse);
			throw e;
		} catch (JsonProcessingException e) {
			log.error("Error convirtiendo a JSON: {}", e.getMessage());
			throw e;
		}
	}

	/**
	 * Parsea respuesta XML y retorna el DTO
	 * 
	 * @param xmlResponse Respuesta XML del servicio JCE
	 * @return DTO parseado o null si hay error
	 */
	public JceResponseDto parseXmlResponse(String xmlResponse) {
		if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
			log.warn("Respuesta XML vacía del servicio JCE");
			return null;
		}

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(JceResponseDto.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			JceResponseDto response = (JceResponseDto) unmarshaller.unmarshal(new StringReader(xmlResponse));

			log.debug("Respuesta XML parseada exitosamente");
			return response;

		} catch (JAXBException e) {
			log.error("Error parseando respuesta XML del servicio JCE: {}", e.getMessage());
			log.debug("XML recibido: {}",
					xmlResponse.length() > 500 ? xmlResponse.substring(0, 500) + "..." : xmlResponse);
			return null;
		}
	}

	/**
	 * Convierte DTO a JSON string
	 * 
	 * @param responseDto DTO de respuesta JCE
	 * @return JSON string
	 * @throws JsonProcessingException si hay error en la conversión
	 */
	public String convertDtoToJson(JceResponseDto responseDto) throws JsonProcessingException {
		if (responseDto == null) {
			return "{}";
		}

		try {
			return objectMapper.writeValueAsString(responseDto);
		} catch (JsonProcessingException e) {
			log.error("Error convirtiendo DTO a JSON: {}", e.getMessage());
			throw e;
		}
	}

	/**
	 * Información completa de la cédula descompuesta
	 */
	public record CedulaInfo(
			String cedulaCompleta,
			String municipio,
			String secuencia,
			String digitoVerificador,
			String cedulaFormateada,
			boolean digitoVerificadorValido) {

		public static CedulaInfo from(String cedula, CedulaValidationUtils utils) {
			String normalized = utils.normalize(cedula);
			utils.validateCedula(normalized);

			return new CedulaInfo(
					normalized,
					utils.getMunicipioCode(normalized),
					utils.getSecuencia(normalized),
					utils.getDigitoVerificador(normalized),
					utils.format(normalized),
					utils.isValidDigitoVerificador(normalized));
		}
	}

	/**
	 * Resultado de conversión XML a JSON
	 */
	public record XmlToJsonResult(
			boolean success,
			String jsonResponse,
			String errorMessage,
			JceResponseDto parsedData) {

		public static XmlToJsonResult success(String json, JceResponseDto dto) {
			return new XmlToJsonResult(true, json, null, dto);
		}

		public static XmlToJsonResult error(String errorMessage) {
			return new XmlToJsonResult(false, "{}", errorMessage, null);
		}
	}

	/**
	 * Método integrado que parsea XML y retorna resultado completo
	 *
	 * @param xmlResponse Respuesta XML del servicio JCE
	 * @return Resultado de la conversión con datos parseados
	 */
	public XmlToJsonResult processJceXmlResponse(String xmlResponse) {
		try {
			JceResponseDto dto = parseXmlResponse(xmlResponse);
			if (dto == null) {
				return XmlToJsonResult.error("No se pudo parsear la respuesta XML");
			}

			String json = convertDtoToJson(dto);
			return XmlToJsonResult.success(json, dto);

		} catch (JsonProcessingException e) {
			log.error("Error procesando respuesta JCE: {}", e.getMessage());
			return XmlToJsonResult.error("Error convirtiendo a JSON: " + e.getMessage());
		}
	}
}
